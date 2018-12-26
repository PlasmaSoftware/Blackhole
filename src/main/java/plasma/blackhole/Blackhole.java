package plasma.blackhole;

import plasma.blackhole.api.NotDecoratedException;
import plasma.blackhole.api.annotations.Decorated;
import plasma.blackhole.util.Indexer;
import plasma.blackhole.util.ResourceUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Blackhole {

    private final static Indexer<String> index;
    // Helps reduce potentially expensive forward lookups
    private final static Map<Class, Class> cache = new ConcurrentHashMap<>(); //TODO: eviction policy?

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

        if (cache.containsKey(clazz))
            return cache.get(clazz);

        String fqn = clazz.getCanonicalName();
        String decorated = index.forwardLookup(fqn);
        try {
            Class decoratedType = Class.forName(decorated);
            cache.put(clazz, decoratedType);
            return decoratedType;
        } catch (ClassNotFoundException e) {
            throw new NotDecoratedException("Class " + clazz + " is not decorated!");
        }
    }

    // TODO Runtime object proxy?
}
