package plasma.blackhole.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class ClassUtils {

    public static <T> T instantiate(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPrimitiveWrapper(Class<?> c) {
        return Byte.class.isAssignableFrom(c)
                || Boolean.class.isAssignableFrom(c)
                || Character.class.isAssignableFrom(c)
                || Short.class.isAssignableFrom(c)
                || Integer.class.isAssignableFrom(c)
                || Long.class.isAssignableFrom(c)
                || Float.class.isAssignableFrom(c)
                || Double.class.isAssignableFrom(c)
                || Void.class.isAssignableFrom(c);
    }
}
