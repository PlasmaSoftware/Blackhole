package plasma.blackhole.api;

import plasma.blackhole.api.annotations.CompileTimeOnly;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.*;

@RequireNoArgConstructor //@Inherited annotations are not detected from interface implementations
public abstract class ClassDecoratorDriver implements DecoratorDriver {

    ClassDecoratorDriver() {}

    @Override
    @CompileTimeOnly
    public abstract void compileInit(JavaFileBatch jfb);

    @Override
    @CompileTimeOnly
    public abstract DecoratorSpec decoratorSpec();

    @Override
    @CompileTimeOnly
    public abstract FieldDefinition[] addFields();

    @CompileTimeOnly
    public abstract MethodDefinition[] addMethods();

    @CompileTimeOnly
    public abstract String[] implementInterfaces();

    // Just hooks, to prevent breakage

    @Override
    @RunTimeOnly
    public abstract void runtimeInit(FieldProxy proxy);

    @RunTimeOnly
    public abstract void init(FieldProxy fieldProxy, MethodProxy methodProxy, Object... args);

    // Total wrap

    @Override
    @RunTimeOnly
    public abstract Object methodWrap(FieldProxy fieldProxy, MethodProxy methodProxy, MethodBinding original, Object... args);
}
