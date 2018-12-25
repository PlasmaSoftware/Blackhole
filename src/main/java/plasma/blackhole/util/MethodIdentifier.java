package plasma.blackhole.util;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MethodIdentifier {

    private final String name;
    private final Class<?>[] argTypes;

    public static MethodIdentifier from(String name, Class<?>[] argTypes) {
        return new MethodIdentifier(name, argTypes);
    }

    public static MethodIdentifier from(String name) {
        return from(name, new Class[0]);
    }

    public static MethodIdentifier from(Method method) {
        return from(method.getName(), method.getParameterTypes());
    }

    private MethodIdentifier(String name, Class<?>[] argTypes) {
        this.name = name;
        this.argTypes = argTypes;
    }

    public String getName() {
        return name;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodIdentifier)) {
            return false;
        }
        MethodIdentifier that = (MethodIdentifier) o;
        return getName().equals(that.getName()) &&
                Arrays.equals(getArgTypes(), that.getArgTypes());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getName());
        result = 31 * result + Arrays.hashCode(getArgTypes());
        return result;
    }
}
