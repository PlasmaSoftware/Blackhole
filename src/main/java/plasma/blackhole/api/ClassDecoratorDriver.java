package plasma.blackhole.api;

import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.util.*;

@RequireNoArgConstructor //@Inherited annotations are not detected from interface implementations
public abstract class ClassDecoratorDriver implements DecoratorDriver {

    public ClassDecoratorDriver() {}

    // Just hooks, to prevent breakage

    @RunTimeOnly
    public abstract void runtimeInit(AnnotationDefinition decorator, Class<?> clazz, FieldProxy proxy);

    @RunTimeOnly
    public abstract void init(AnnotationDefinition decorator, Class<?> clazz, FieldProxy fieldProxy,
                              MethodProxy methodProxy, Object... args);

    // Total wrap

    @Override
    @RunTimeOnly
    public abstract Object methodWrap(AnnotationDefinition decorator, Class<?> clazz, FieldProxy fieldProxy,
                                      MethodProxy methodProxy, MethodBinding original, Object... args);
}
