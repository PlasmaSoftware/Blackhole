package plasma.blackhole.api;

import plasma.blackhole.api.annotations.CompileTimeOnly;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.FieldDefinition;
import plasma.blackhole.util.MethodDefinition;

/**
 * @implNote You should attempt to limit imports of classes in the same source module in implementations of this hook.
 *  This is because, due to the limitations of annotation processing, Blackhole will use a primitive bootstrapping
 *  java compiler to load the implementation directly from source code. The more intra-module imports, the longer this
 *  will take and the higher chance of hitting a limitation on the compiler. <b>Usage of pre-compiled compile-time available
 *  library classes are perfectly though!</b>
 */
@RequireNoArgConstructor
public abstract class CompileTimeHook {  //Abstract to help prevent dangerous usages by combining with other drivers

    @CompileTimeOnly
    public abstract void compileInit(JavaFileBatch jfb);

    @CompileTimeOnly
    public abstract FieldDefinition[] addFields();

    @CompileTimeOnly
    public abstract MethodDefinition[] addMethods();

    @CompileTimeOnly
    public abstract String[] implementInterfaces();
}
