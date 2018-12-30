package ${package};

import javax.annotation.Generated;
import java.lang.annotation.*;

@Generated("plasma.blackhole.DecoratorAnnotationProcessor")
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.${retention})
public @interface ${name} {
    ${annotation_body}
}