package plasma.blackhole.util;

import java.util.HashMap;
import java.util.Map;

public class FieldProxy {

    private final Map<String, FieldBinding> bindings = new HashMap<>();

    public FieldProxy() {

    }

    public FieldProxy(FieldProxy inherits) {
        this();
        bindings.putAll(inherits.bindings);
    }

    public FieldProxy bind(String name, FieldBinding binding) {
        bindings.put(name, binding);
        return this;
    }

    public FieldBinding getBinding(String name) {
        return bindings.get(name);
    }
}
