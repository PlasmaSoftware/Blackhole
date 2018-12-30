package plasma.blackhole;

import plasma.blackhole.api.NotDecoratedException;
import plasma.blackhole.api.annotations.Decorated;
import plasma.blackhole.util.ConstructorProxy;
import plasma.blackhole.util.Indexer;
import plasma.blackhole.util.MethodBinding;
import plasma.blackhole.util.internal.ResourceUtils;
import plasma.blackhole.util.VarargFunction;
import plasma.blackhole.util.support.FieldGetter;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Blackhole {

    private final static Indexer<String> index;
    // Helps reduce potentially expensive lookups
    //TODO: eviction policy?
    private final static Map<Class, Class> decorationCache = new ConcurrentHashMap<>();
    private final static Map<Class, ConstructorProxy> constructorsGetterCache = new ConcurrentHashMap<>();

    static {
        index = Indexer.readStringIndex(ResourceUtils.readFileOrEmpty("blackhole/decorated.idx"));
    }

    public static boolean isDecorated(Class<?> clazz) {
        return clazz.isAnnotationPresent(Decorated.class);
    }

    public static boolean isDecorated(Object o) {
        if (o == null) return false;

        return isDecorated(o.getClass());
    }

    public static Class<?> getOriginalClass(Class<?> clazz) {
        return isDecorated(clazz) ? clazz.getAnnotation(Decorated.class).originalClass() : clazz;
    }

    public static Class<?> getOriginalClass(Object o) {
        if (o == null) throw new NullPointerException("Expecting a non-null parameter!");

        return getOriginalClass(o.getClass());
    }

    public static <T> Class<? extends T> decorate(Class<T> clazz) throws NotDecoratedException {
        if (isDecorated(clazz)) return clazz;

        if (decorationCache.containsKey(clazz))
            return decorationCache.get(clazz);

        String fqn = clazz.getCanonicalName();
        String decorated = index.forwardLookup(fqn);
        try {
            Class decoratedType = Class.forName(decorated);
            decorationCache.put(clazz, decoratedType);
            return decoratedType;
        } catch (ClassNotFoundException e) {
            throw new NotDecoratedException("Class " + clazz + " is not decorated!");
        }
    }

    public static <T> VarargFunction<Object, ? extends T> constructors(Class<? extends T> clazz) {
        if (!isDecorated(clazz))
            clazz = decorate(clazz);

        final ConstructorProxy constructorsGetter;
        if (constructorsGetterCache.containsKey(clazz))
            constructorsGetter = constructorsGetterCache.get(clazz);
        else {
            constructorsGetter = FieldGetter.makeGetter(clazz, "__CONSTRUCTOR_PROXY__", ConstructorProxy.class)
                    .apply(null);
            constructorsGetterCache.put(clazz, constructorsGetter);
        }

        //noinspection unchecked
        return (VarargFunction<Object, ? extends T>) new VarargFunction() {

            @SuppressWarnings("unchecked")
            @Override
            public T apply(Object... o) {
                MethodBinding binding = Objects.requireNonNull(constructorsGetter.bestGuessBinding(o));
                return (T) binding.invoke(o);
            }

            @Override //:thonking:
            public Object apply(Object o) {
                return apply(new Object[]{o});
            }
        };
    }
}
