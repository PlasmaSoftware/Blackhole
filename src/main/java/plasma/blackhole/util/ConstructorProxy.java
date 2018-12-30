package plasma.blackhole.util;

// Wrapper around MethodProxy to make names not required as they don't make sense in this context
public class ConstructorProxy extends MethodProxy {

    private final static String CONSTRUCTOR_NAME = "<init>";

    private ConstructorProxy(MethodProxy inherits) { //No reason for this anymore
        super(inherits);
    }

    public MethodProxy bind(MethodBinding binding) {
        return super.bind(CONSTRUCTOR_NAME, binding);
    }

    public MethodBinding getBinding(Class<?>... argTypes) {
        return super.getBinding(CONSTRUCTOR_NAME, argTypes);
    }

    public MethodBinding bestGuessBinding(Object... argInstances) {
        return super.bestGuessBinding(CONSTRUCTOR_NAME, argInstances);
    }
}
