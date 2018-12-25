package plasma.blackhole.util;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class FieldDefinition {

    private final String name;
    private final int modifiers;
    private final Class<?> type;
    private final AnnotationDefinition[] annotations;

    private FieldDefinition(String name, int modifiers, Class<?> type, AnnotationDefinition[] annotations) {
        this.name = name;
        this.modifiers = modifiers;
        this.type = type;
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

    public Class<?> getType() {
        return type;
    }

    public AnnotationDefinition[] getAnnotations() {
        return annotations;
    }

    public String builderCode() {
        return "plasma.blackhole.util.FieldDefinition.builder(" +
                getName() +
                ", " +
                getType().getCanonicalName() +
                ").addModifier(" +
                modifiers +
                ").setAnnotations(new plasma.blackhole.util.AnnotationDefinition[]{" +
                Arrays.stream(annotations).map(AnnotationDefinition::builderCode).collect(Collectors.joining(",")) +
                "}).build();";
    }

    public static Builder builder(String name, Class<?> type) {
        return new Builder(name, type);
    }

    //Ignore annotations
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FieldDefinition)) {
            return false;
        }
        FieldDefinition that = (FieldDefinition) o;
        return getModifiers() == that.getModifiers() &&
                getName().equals(that.getName()) &&
                getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getModifiers(), getType());
    }

    public static class Builder {

        private final String name;
        private final Class<?> type;
        private int modifiers = 0;
        private AnnotationDefinition[] annotations = new AnnotationDefinition[0];

        public Builder(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        public Builder addModifier(int modifier) {
            this.modifiers |= modifier;
            return this;
        }

        public Builder setStatic() {
            return addModifier(Modifier.STATIC);
        }

        public Builder setAnnotations(AnnotationDefinition[] annotations) {
            this.annotations = annotations;
            return this;
        }

        public FieldDefinition build() {
            return new FieldDefinition(name, modifiers, type, annotations);
        }
    }
}
