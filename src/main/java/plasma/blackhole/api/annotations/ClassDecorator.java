package plasma.blackhole.api.annotations;

import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.CompileTimeHook;
import plasma.blackhole.api.NoOpCompileTimeHook;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface ClassDecorator {

    Class<? extends ClassDecoratorDriver> value();

    Class<? extends CompileTimeHook> onCompile() default NoOpCompileTimeHook.class;
}
