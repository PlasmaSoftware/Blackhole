package plasma.blackhole.api;

import plasma.blackhole.api.annotations.CompileTimeOnly;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.*;

//TODO hook in

@RequireNoArgConstructor //@Inherited annotations are not detected from interface implementations
public abstract class MethodDecoratorDriver implements DecoratorDriver {

    MethodDecoratorDriver() {}

    @Override
    @CompileTimeOnly
    public abstract void compileInit(JavaFileBatch jfb);

    @Override
    @CompileTimeOnly
    public abstract DecoratorSpec decoratorSpec();

    @Override
    @CompileTimeOnly
    public abstract FieldDefinition[] addFields();

    @Override
    @RunTimeOnly
    public abstract void runtimeInit(FieldProxy proxy);

    // Total wrap

    @Override
    @RunTimeOnly
    public abstract Object methodWrap(FieldProxy fieldProxy, MethodProxy methodProxy, MethodBinding original, Object... args);
}
