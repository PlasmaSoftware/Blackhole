package plasma.blackhole.api;

import plasma.blackhole.api.annotations.CompileTimeOnly;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.DecoratorSpec;

@RequireNoArgConstructor //@Inherited annotations are not detected from interface implementations
public abstract class ClassDecoratorDriver {

    @CompileTimeOnly
    public abstract void compileInit(JavaFileBatch jfb);

    @CompileTimeOnly
    public abstract DecoratorSpec decoratorSpec();

    @RunTimeOnly
    public abstract void runtimeInit();
}
