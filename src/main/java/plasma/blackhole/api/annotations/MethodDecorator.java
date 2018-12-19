package plasma.blackhole.api.annotations;

import plasma.blackhole.api.MethodDecoratorDriver;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface MethodDecorator {

    Class<? extends MethodDecoratorDriver> driver();
}
