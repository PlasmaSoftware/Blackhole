package plasma.blackhole.util;

import java.lang.annotation.RetentionPolicy;

//TODO: Add potential fields for metadata
public class DecoratorSpec {

    private final String pkg, name;
    private final RetentionPolicy retention;

    private DecoratorSpec(String pkg, String name, RetentionPolicy retention) {
        this.pkg = pkg;
        this.name = name;
        this.retention = retention;
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

    public static Builder builder(String pkg, String name) {
        return new Builder(pkg, name);
    }

    public static class Builder {

        private final String pkg;
        private final String name;
        private RetentionPolicy retention = RetentionPolicy.SOURCE;

        private Builder(String pkg, String name) {
            this.pkg = pkg;
            this.name = name;
        }

        public Builder setRetention(RetentionPolicy policy) {
            this.retention = policy;
            return this;
        }

        public DecoratorSpec build() {
            return new DecoratorSpec(pkg, name, retention);
        }
    }
}
