package plasma.blackhole.processor;

import com.austinv11.servicer.WireService;
import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.MethodDecoratorDriver;
import plasma.blackhole.api.annotations.ClassDecorator;
import plasma.blackhole.api.annotations.MethodDecorator;
import plasma.blackhole.util.ClassUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

@WireService(Processor.class)
public class DecoratorAnnotationProcessor extends AbstractBlackholeAnnotationProcessor {

    private final Class<? extends Annotation>[] annotations = new Class[] {ClassDecorator.class, MethodDecorator.class};

    @Override
    public Class<? extends Annotation>[] annotations() {
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        JavaFileBatch batch = new JavaFileBatch();
        boolean changed = handleClassDecorators(batch, roundEnv.getElementsAnnotatedWith(ClassDecorator.class));
        changed = changed || handleMethodDecorators(batch, roundEnv.getElementsAnnotatedWith(MethodDecorator.class));
        try {
            batch.publish(getFiler());
        } catch (IOException e) {
            error(e);
            return false;
        }
        return changed;
    }

    private boolean handleClassDecorators(JavaFileBatch batch, Set<? extends Element> elements) {
        elements.forEach(element -> {
            try {
                ClassDecorator decorator = element.getAnnotation(ClassDecorator.class);
                ClassDecoratorDriver driver = ClassUtils.instantiate(decorator.driver());
                info("Identified Class Decorator", driver);


            } catch (Exception e) {
                error("Exception caught handling class decorators!", e);
                warning("Attempting to continue...");
            }
        });
    }

    private boolean handleMethodDecorators(JavaFileBatch batch, Set<? extends Element> elements) {
        elements.forEach(element -> {
            try {
                MethodDecorator decorator = element.getAnnotation(MethodDecorator.class);
                MethodDecoratorDriver driver = ClassUtils.instantiate(decorator.driver());
                info("Identified Method Decorator", driver);


            } catch (Exception e) {
                error("Exception caught handling method decorators!", e);
                warning("Attempting to continue...");
            }
        });
    }
}
