package plasma.blackhole.util;

import java.lang.reflect.Modifier;
import java.util.function.Function;

public class MethodBinding {

    private final String name;
    private final int modifiers;
    private final Class<?> returnType;
    private final Class<?>[] argTypes;
    private final Function<Object[], Object> invoker;

    public MethodBinding(String name, int modifiers, Class<?> returnType, Class<?>[] argTypes,
                         Function<Object[], Object> invoker) {
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.argTypes = argTypes;
        this.invoker = invoker;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public Object invoke(Object... args) {
        return invoker.apply(args);
    }
}
