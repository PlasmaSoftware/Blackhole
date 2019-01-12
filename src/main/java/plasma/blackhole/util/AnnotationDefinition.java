package plasma.blackhole.util;

import plasma.blackhole.util.internal.ClassTypeProxy;
import plasma.blackhole.util.internal.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnnotationDefinition {

    private final Class<? extends Annotation> annotation;
    private final Map<String, Object> bindings;

    private static Map<String, Object> makeBindings(Annotation a) {
        Map<String, Object> bindings = new HashMap<>();
        Arrays.stream(a.annotationType().getDeclaredMethods())
                .filter(m -> m.getDeclaringClass().equals(a.annotationType()))
                .forEach(m -> {
                    try {
                        bindings.put(m.getName(), m.invoke(a));
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                });
        return bindings;
    }

    public AnnotationDefinition(Annotation annotation) {
        this(annotation.annotationType(), makeBindings(annotation));
    }

    private AnnotationDefinition(Class<? extends Annotation> annotation, Map<String, Object> bindings) {
        this.annotation = annotation;
        this.bindings = bindings;
    }

    public Class<? extends Annotation> getAnnotation() {
        return annotation;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public static String toAnnotationLiteral(Object o) {
        if (o == null)
            return "null";

        if (o instanceof ClassTypeProxy)
            return ((ClassTypeProxy) o).toLiteral();

        Class<?> type = o.getClass();
        if (type.isArray()) {
            return "{" + IntStream.range(0, Array.getLength(o))
                    .mapToObj(i -> Array.get(o, i))
                    .map(AnnotationDefinition::toAnnotationLiteral)
                    .collect(Collectors.joining(", ")) + "}";
        } else {
            if (type.isPrimitive() || ClassUtils.isPrimitiveWrapper(type)) {
                if (type.equals(char.class) || type.equals(Character.class)) {
                    return "'" + o + "'";
                } else {
                    return String.valueOf(o);
                }
            } else if (type.isEnum()) {
                return type.getCanonicalName() + "." + ((Enum) o).name();
            } else if (type.equals(String.class)) {
                return "\"" + o + "\"";
            } else if (Class.class.isAssignableFrom(type)) {
                return ((Class) o).getCanonicalName() + ".class";
            } else if (type.isAnnotation()) {
                return new AnnotationDefinition((Annotation) o).toString();
            } else if (AnnotationDefinition.class.isAssignableFrom(type)) {
                return o.toString();
            } else {
                throw new AssertionError("Object " + o + " of type " + type + " cannot be used in an annotation!");
            }
        }
    }

    public String builderCode() {
        StringBuilder sb = new StringBuilder()
                .append("plasma.blackhole.util.AnnotationDefinition.builder(").
                        append(annotation.getCanonicalName())
                .append(".class).");

        bindings.forEach((k, v) -> {
            sb.append("bindParameter(\"")
                    .append(k)
                    .append("\", ")
                    .append(v != null && v.getClass().isAnnotation()
                            ? new AnnotationDefinition((Annotation) v).builderCode() : toAnnotationLiteral(v))
                    .append(").");
        });

        return sb.append("build()").toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("@")
                .append(getAnnotation().getCanonicalName())
                .append("(");

        bindings.forEach((k, v) -> {
            sb.append(k).append(" = ").append(toAnnotationLiteral(v)).append(", ");
        });

        return sb.toString();
    }

    //Ignore annotations
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AnnotationDefinition)) {
            return false;
        }
        AnnotationDefinition that = (AnnotationDefinition) o;
        return getAnnotation().equals(that.getAnnotation()) &&
                getBindings().equals(that.getBindings());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAnnotation(), getBindings());
    }

    public static Builder builder(Class<? extends Annotation> clazz) {
        return new Builder(clazz);
    }

    public static class Builder {

        private final Class<? extends Annotation> clazz;
        private final Map<String, Object> bindings = new HashMap<>();

        public Builder(Class<? extends Annotation> clazz) {
            this.clazz = clazz;
        }

        public Builder bindParameter(String key, Object o) {
            bindings.put(key, o);
            return this;
        }

        public AnnotationDefinition build() {
            return new AnnotationDefinition(clazz, bindings);
        }
    }
}
