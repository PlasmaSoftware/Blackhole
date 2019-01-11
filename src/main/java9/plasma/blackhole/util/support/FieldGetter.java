package plasma.blackhole.util.support;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

//Java 9+ field getter
public class FieldGetter {

    public static <O, T> Function<O, T> makeGetter(Class<O> clazz, String fieldName, Class<T> fieldType) {
        return new Function<>() {

            private final VarHandle f;

            {
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                    f = lookup.unreflectVarHandle(field);
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
