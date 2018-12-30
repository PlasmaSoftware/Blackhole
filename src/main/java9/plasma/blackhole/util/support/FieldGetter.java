package plasma.blackhole.util.support;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.function.Function;

//Java 9+ field getter
public class FieldGetter {

    public static <O, T> Function<O, T> makeGetter(Class<O> clazz, String fieldName, Class<T> fieldType) {
        return new Function<>() {

            private final VarHandle f;

            {
                try {
                    f = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup())
                            .findVarHandle(clazz, fieldName, fieldType);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T apply(O obj) {
                return obj == null ? (T) f.get() : (T) f.get(obj);
            }
        };
    }
}
