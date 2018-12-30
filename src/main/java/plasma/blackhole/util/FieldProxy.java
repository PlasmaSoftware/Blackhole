package plasma.blackhole.util;

import java.util.HashMap;
import java.util.Map;

//Really simple map wrapper, but it is needed to provide an API consistent with MethodProxy and to allow for potential
//changes to be more easily adopted
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
