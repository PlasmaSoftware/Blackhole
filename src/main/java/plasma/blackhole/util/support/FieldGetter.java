package plasma.blackhole.util.support;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;

// Java 8 field getter
public class FieldGetter {

    public static <O, T> Function<O, T> makeGetter(Class<O> clazz, String fieldName, Class<T> fieldType) {
        return new Function<O, T>() {

            private final MethodHandle fg;
            private final boolean isStatic;

            {
                try {
                    Field f = clazz.getDeclaredField(fieldName);
                    f.setAccessible(true);
                    MethodHandles.Lookup lookup = MethodHandles.lookup().in(clazz);
                    isStatic = Modifier.isStatic(f.getModifiers());
                    fg = lookup.unreflectGetter(f);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public T apply(O obj) {
                try {
                    return isStatic ? (T) fg.invoke() : (T) fg.bindTo(obj).invoke();
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            }
        };
    }
}
