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
}
