package plasma.blackhole.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class MethodDefinition {

    private final String name;
    private final int modifiers;
    private final Class<?> returnType;
    private final Class<?>[] argTypes;
    //TODO: parameter annotations
    private final AnnotationDefinition[] annotations;

    public MethodDefinition(String name, int modifiers, Class<?> returnType, Class<?>[] argTypes,
                            AnnotationDefinition[] annotations) {
        this.name = name;
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.argTypes = argTypes;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isStatic() {
        return Modifier.isStatic(modifiers);
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public AnnotationDefinition[] getAnnotations() {
        return annotations;
    }

    public String builderCode() {
        return "plasma.blackhole.util.MethodDefinition.builder(" +
                getName() +
                ").addModifier(" +
                modifiers +
                ").setReturnType(" +
                returnType.getCanonicalName() +
                ".class).setArgTypes(new Class[]{" +
                Arrays.stream(argTypes).map(a -> a.getCanonicalName() + ".class").collect(Collectors.joining(",")) +
                "}).setAnnotations(new plasma.blackhole.util.AnnotationDefinition[]{" +
                Arrays.stream(annotations).map(AnnotationDefinition::builderCode).collect(Collectors.joining(",")) +
                "}).build();";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodDefinition)) {
            return false;
        }
        MethodDefinition that = (MethodDefinition) o;
        return getModifiers() == that.getModifiers() &&
                getName().equals(that.getName()) &&
                getReturnType().equals(that.getReturnType()) &&
                Arrays.equals(getArgTypes(), that.getArgTypes());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getName(), getModifiers(), getReturnType());
        result = 31 * result + Arrays.hashCode(getArgTypes());
        return result;
    }

    public static MethodDefinition from(Method m) {
        AnnotationDefinition[] annotations = new AnnotationDefinition[m.getAnnotations().length];
        for (int i = 0; i < annotations.length; i++) {
            annotations[i] = new AnnotationDefinition(m.getAnnotations()[i]);
        }
        return new MethodDefinition(m.getName(), m.getModifiers(), m.getReturnType(), m.getParameterTypes(), annotations);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;
        private int modifiers = 0;
        private Class<?> returnType = void.class;
        private Class<?>[] argTypes = new Class[0];
        private AnnotationDefinition[] annotations = new AnnotationDefinition[0];

        private Builder(String name) {
            this.name = name;
        }

        public Builder addModifier(int modifier) {
            this.modifiers |= modifier;
            return this;
        }

        public Builder setStatic() {
            return addModifier(Modifier.STATIC);
        }

        public Builder setReturnType(Class<?> returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder setArgTypes(Class<?>[] argTypes) {
            this.argTypes = argTypes;
            return this;
        }

        public Builder setAnnotations(AnnotationDefinition[] annotations) {
            this.annotations = annotations;
            return this;
        }

        public MethodDefinition build() {
            return new MethodDefinition(name, modifiers, returnType, argTypes, annotations);
        }
    }
}
