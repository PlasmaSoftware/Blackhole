package plasma.blackhole.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

//TODO: non-boxing primitive support
public class FieldBinding {

    private final String name;
    private final boolean isStatic;
    private final int modifiers;
    private final Class type;
    private final Supplier getter;
    private final Consumer setter;

    public FieldBinding(String name,
                        boolean isStatic,
                        int modifiers,
                        Class type,
                        Supplier getter,
                        Consumer setter) {
        this.name = name;
        this.isStatic = isStatic;
        this.modifiers = modifiers;
        this.type = type;
        this.getter = getter;
        this.setter = setter;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public int getModifiers() {
        return modifiers;
    }

    public Class getType() {
        return type;
    }

    public <T> T get() {
        return (T) getter.get();
    }

    public <T> void set(T val) {
        setter.accept(val);
    }
}
