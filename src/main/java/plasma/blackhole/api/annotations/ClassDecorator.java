package plasma.blackhole.api.annotations;

import plasma.blackhole.api.ClassDecoratorDriver;

import java.lang.annotation.*;

@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface ClassDecorator {

    Class<? extends ClassDecoratorDriver> driver();
}
