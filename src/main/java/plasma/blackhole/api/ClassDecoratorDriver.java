package plasma.blackhole.api;

import plasma.blackhole.api.annotations.CompileTimeOnly;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.DecoratorSpec;

@RequireNoArgConstructor
public interface ClassDecoratorDriver {

    @CompileTimeOnly
    void compileInit(JavaFileBatch jfb);

    @CompileTimeOnly
    DecoratorSpec decoratorSpec();

    @RunTimeOnly
    void runtimeInit();

    @RunTimeOnly

}
