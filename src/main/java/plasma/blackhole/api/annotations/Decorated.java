package plasma.blackhole.api.annotations;

import java.lang.annotation.*;

@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Decorated {

    Class<?> originalClass();
}
