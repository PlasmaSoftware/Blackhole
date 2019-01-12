package plasma.blackhole.util.internal;

import java.lang.reflect.Array;
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

    public static boolean isAssignableFrom(Class<?> target, Class<?> from) {
        return target.isAssignableFrom(from)
                || (isPrimitiveWrapper(target) && checkPrimitiveWrapperCompat(target, from) )
                || (isPrimitiveWrapper(from) && checkPrimitiveWrapperCompat(from, target));
    }

    private static boolean checkPrimitiveWrapperCompat(Class<?> clazz1, Class<?> clazz2) {
        if (clazz1.equals(Byte.class)) {
            return clazz2.equals(byte.class);
        } else if (clazz1.equals(Boolean.class)) {
            return clazz2.equals(boolean.class);
        } else if (clazz1.equals(Character.class)) {
            return clazz2.equals(char.class);
        } else if (clazz1.equals(Short.class)) {
            return clazz2.equals(short.class);
        } else if (clazz1.equals(Integer.class)) {
            return clazz2.equals(int.class);
        } else if (clazz1.equals(Long.class)) {
            return clazz2.equals(long.class);
        } else if (clazz1.equals(Float.class)) {
            return clazz2.equals(float.class);
        } else if (clazz1.equals(Double.class)) {
            return clazz2.equals(double.class);
        } else if (clazz1.equals(Void.class)) {
            return clazz2.equals(void.class);
        } else {
            return false;
        }
    }

    public static Class<?> stringToClass(String s) {
        try {
            if (s.endsWith("[]")) {
                return Array.newInstance(stringToClass(s.substring(0, s.length()-2)), 0).getClass();
            }
            return Class.forName(s);
        } catch (Exception e) {
            switch (s) {
                case "byte":
                    return byte.class;
                case "boolean":
                    return boolean.class;
                case "char":
                    return char.class;
                case "short":
                    return short.class;
                case "int":
                    return int.class;
                case "long":
                    return long.class;
                case "float":
                    return float.class;
                case "double":
                    return double.class;
                case "void":
                    return void.class;
                default:
                    throw new RuntimeException(e);
            }
        }
    }
}
