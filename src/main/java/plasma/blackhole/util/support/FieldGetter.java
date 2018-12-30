package plasma.blackhole.util.support;

import java.lang.reflect.Field;
import java.util.function.Function;

// Java 8 field getter
public class FieldGetter {

    public static <O, T> Function<O, T> makeGetter(Class<O> clazz, String fieldName, Class<T> fieldType) {
        return new Function<O, T>() {

            private final Field f;

            {
                try {
                    f = clazz.getField(fieldName);
                    f.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T apply(O obj) {
                try {
                    return (T) f.get(obj);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
