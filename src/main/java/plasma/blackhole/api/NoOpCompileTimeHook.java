package plasma.blackhole.api;

import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.FieldDefinition;
import plasma.blackhole.util.MethodDefinition;

public final class NoOpCompileTimeHook extends CompileTimeHook {

    @Override
    public void compileInit(JavaFileBatch jfb) {}

    @Override
    public FieldDefinition[] addFields() {
        return new FieldDefinition[0];
    }

    @Override
    public MethodDefinition[] addMethods() {
        return new MethodDefinition[0];
    }

    @Override
    public String[] implementInterfaces() {
        return new String[0];
    }
}
