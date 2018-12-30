package plasma.blackhole.util;

import plasma.blackhole.util.internal.AttributeSearchTree;
import plasma.blackhole.util.internal.ClassUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MethodProxy {

    private final static Function<MethodBinding, Class[]> ATTR_EXTRACTOR = MethodBinding::getArgTypes;

    private final Map<String, AttributeSearchTree<Class, MethodBinding>> bindings = new HashMap<>();

    public MethodProxy() {

    }

    public MethodProxy(MethodProxy inherits) {
        this();
        bindings.putAll(inherits.bindings);
    }

    public MethodProxy bind(String name, MethodBinding binding) {
        if (bindings.containsKey(name)) {
            bindings.get(name).addObject(binding);
        } else {
            AttributeSearchTree<Class, MethodBinding> ast = new AttributeSearchTree<>(ATTR_EXTRACTOR);
            ast.addObject(binding);
            bindings.put(name, ast);
        }
        return this;
    }

    public MethodBinding getBinding(String name, Class<?>... argTypes) {
        return bindings.containsKey(name) ? bindings.get(name).findObject(argTypes) : null;
    }

    public MethodBinding bestGuessBinding(String name, Object... argInstances) {
        if (!bindings.containsKey(name)) return null;

        AttributeSearchTree<Class, MethodBinding> ast = bindings.get(name);

        return ast.interactiveTraversal((choices, level, levelVisitedCount) -> {
            if (argInstances.length > level) {
                Object arg = argInstances[level];
                List<Class> prunedChoices;
                if (arg == null) //Shit, it could be anything except a primitive
                    prunedChoices = choices.stream()
                            .filter(c -> c != AttributeSearchTree.EMPTY)
                            .map(c -> (Class) c)
                            .filter(c -> !c.isPrimitive())
                            .sorted() //Ensure deterministic order
                            .collect(Collectors.toList());

                else
                    prunedChoices = choices.stream()
                            .filter(c -> c != AttributeSearchTree.EMPTY)
                            .map(c -> (Class) c)
                            .filter(c -> ClassUtils.isAssignableFrom(c, arg.getClass()))
                            .sorted() //Ensure deterministic order
                            .collect(Collectors.toList());

                if (prunedChoices.isEmpty() || levelVisitedCount == prunedChoices.size()) {
                    return null; //Dead end
                } else {
                    return prunedChoices.get(levelVisitedCount);
                }
            } else {
                if (level == argInstances.length)
                    return AttributeSearchTree.EMPTY; //This should be the destination

                throw new AssertionError(); //This should never happen
            }
        });
    }

    public MethodBinding getBinding(MethodIdentifier info) {
        return getBinding(info.getName(), info.getArgTypes());
    }
}
