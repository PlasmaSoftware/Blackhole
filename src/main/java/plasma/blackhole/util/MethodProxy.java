package plasma.blackhole.util;

import java.util.HashMap;
import java.util.Map;

public class MethodProxy {

    private final Map<MethodIdentifier, MethodBinding> bindings = new HashMap<>();

    public MethodProxy() {

    }

    public MethodProxy(MethodProxy inherits) {
        this();
        bindings.putAll(inherits.bindings);
    }

    public MethodProxy bind(MethodIdentifier name, MethodBinding binding) {
        bindings.put(name, binding);
        return this;
    }

    public MethodBinding getBinding(MethodIdentifier name) {
        return bindings.get(name);
    }
}
