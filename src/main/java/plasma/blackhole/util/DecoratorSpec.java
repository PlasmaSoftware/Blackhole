package plasma.blackhole.util;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public class DecoratorSpec {

    private final String pkg, name;
    private final RetentionPolicy retention;
    private final AnnotationProperty[] properties;

    private DecoratorSpec(String pkg, String name, RetentionPolicy retention, AnnotationProperty[] properties) {
        this.pkg = pkg;
        this.name = name;
        this.retention = retention;
        this.properties = properties;
    }

    public String getPackage() {
        return pkg;
    }

    public String getName() {
        return name;
    }

    public RetentionPolicy retentionPolicy() {
        return retention;
    }

    public AnnotationProperty[] getProperties() {
        return properties;
    }

    public static Builder builder(String pkg, String name) {
        return new Builder(pkg, name);
    }

    public static class Builder {

        private final String pkg;
        private final String name;
        private RetentionPolicy retention = RetentionPolicy.SOURCE;
        private final List<AnnotationProperty> properties = new ArrayList<>();

        private Builder(String pkg, String name) {
            this.pkg = pkg;
            this.name = name;
        }

        public Builder setRetention(RetentionPolicy policy) {
            this.retention = policy;
            return this;
        }

        public Builder addProperty(AnnotationProperty property) {
            properties.add(property);
            return this;
        }

        public DecoratorSpec build() {
            return new DecoratorSpec(pkg, name, retention, properties.toArray(new AnnotationProperty[0]));
        }
    }

    public static class AnnotationProperty {

        private final String name;
        private final Class<?> type;
        private final Object defaultObj;

        public AnnotationProperty(String name, Class<?> type, Object defaultObj) {
            this.name = name;
            this.type = type;
            this.defaultObj = defaultObj;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public Object getDefault() {
            return defaultObj;
        }
    }
}
