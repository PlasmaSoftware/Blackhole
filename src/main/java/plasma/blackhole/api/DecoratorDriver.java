package plasma.blackhole.api;

import plasma.blackhole.api.annotations.CompileTimeOnly;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.api.annotations.RunTimeOnly;
import plasma.blackhole.processor.JavaFileBatch;
import plasma.blackhole.util.*;

@RequireNoArgConstructor
interface DecoratorDriver { //Impls need to be manually hooked

    @CompileTimeOnly
    void compileInit(JavaFileBatch jfb);

    @CompileTimeOnly
    DecoratorSpec decoratorSpec();

    @CompileTimeOnly
    FieldDefinition[] addFields();

    //TODO: Pass decorated class info

    @RunTimeOnly
    void runtimeInit(AnnotationDefinition decorator, Class<?> clazz, FieldProxy proxy);

    // Total wrap

    @RunTimeOnly
    Object methodWrap(AnnotationDefinition decorator, Class<?> clazz, FieldProxy fieldProxy, MethodProxy methodProxy,
                      MethodBinding original, Object... args);
}
