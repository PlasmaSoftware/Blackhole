package plasma.blackhole.api.annotations;

import java.lang.annotation.*;

/**
 * When a class is annotated with this annotation, the
 * {@link plasma.blackhole.processor.RequireNoArgConstructorAnnotationProcessor} annotation processor runs a
 * compile-time check on all classes which extend the class with this annotation to ensure that it contains a public
 * no-arg constructor.
 *
 * @implNote Due to limitations in annotation processing and the implementation of the
 * {@link java.lang.annotation.Inherited} meta-annotation: this annotation is only guaranteed to work when placed on
 * a class--not interfaces.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface RequireNoArgConstructor {
}
