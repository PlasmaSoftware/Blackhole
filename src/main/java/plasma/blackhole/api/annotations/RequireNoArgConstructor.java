package plasma.blackhole.api.annotations;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RequireNoArgConstructor {
}
