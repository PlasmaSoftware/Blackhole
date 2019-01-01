package plasma.blackhole.api.annotations;

import plasma.blackhole.api.CompileTimeHook;
import plasma.blackhole.api.MethodDecoratorDriver;
import plasma.blackhole.api.NoOpCompileTimeHook;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface MethodDecorator {

    Class<? extends MethodDecoratorDriver> value();

    Class<? extends CompileTimeHook> onCompile() default NoOpCompileTimeHook.class;
}
