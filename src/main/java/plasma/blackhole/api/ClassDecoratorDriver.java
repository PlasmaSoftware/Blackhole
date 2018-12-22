package plasma.blackhole.api;

import plasma.blackhole.api.annotations.CompileTimeOnly;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.*;

@RequireNoArgConstructor //@Inherited annotations are not detected from interface implementations
public abstract class ClassDecoratorDriver {

    @CompileTimeOnly
    public abstract void compileInit(JavaFileBatch jfb);

    @CompileTimeOnly
    public abstract DecoratorSpec decoratorSpec();

    @CompileTimeOnly
    public abstract FieldSpec[] addFields();

    @CompileTimeOnly
    public abstract MethodSpec[] addMethods();

    @CompileTimeOnly
    public abstract boolean explicitOnly();

    @RunTimeOnly
    public abstract void runtimeInit();

    // Just hooks, to prevent breakage

    @RunTimeOnly
    public abstract void cinit(FieldProxy proxy);

    @RunTimeOnly
    public abstract void init(FieldProxy fieldProxy, MethodProxy methodProxy, Object... args);

    // Total wrap

    @RunTimeOnly
    public abstract Object methodWrap(FieldProxy fieldProxy, MethodProxy methodProxy, MethodBinding original, Object... args);
}
