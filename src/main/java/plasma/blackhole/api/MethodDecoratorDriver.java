package plasma.blackhole.api;

import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.util.*;

@RequireNoArgConstructor //@Inherited annotations are not detected from interface implementations
public abstract class MethodDecoratorDriver implements DecoratorDriver {

    public MethodDecoratorDriver() {}

    @RunTimeOnly
    public abstract void runtimeInit(Class<?> clazz, FieldProxy proxy);

    // Total wrap

    @Override
    @RunTimeOnly
    public abstract Object methodWrap(AnnotationDefinition decorator, Class<?> clazz, FieldProxy fieldProxy,
                                      MethodProxy methodProxy, MethodBinding original, Object... args);
}
