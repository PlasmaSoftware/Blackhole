package plasma.blackhole.processor;

import com.austinv11.servicer.WireService;
import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.MethodDecoratorDriver;
import plasma.blackhole.api.annotations.ClassDecorator;
import plasma.blackhole.api.annotations.MethodDecorator;
import plasma.blackhole.util.*;
import plasma.blackhole.util.internal.ClassUtils;
import plasma.blackhole.util.internal.ResourceUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

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

    private String getBody(DecoratorSpec.AnnotationProperty property) {
        String base = property.getType().getCanonicalName() + " " + property.getName() + "()";
        if (property.getDefault() == null) {
            return base + ";";
        } else {
            return base + " default " + AnnotationDefinition.toAnnotationLiteral(property.getDefault()) + ";";
        }
    }

    private boolean handleClassDecorators(JavaFileBatch batch, Set<? extends Element> elements) {
        String processorTemplate = ResourceUtils.readFile("blackhole/templates/ClassDecoratorProcessorTemplate.java");
        String annotationTemplate = ResourceUtils.readFile("blackhole/templates/ClassDecoratorTemplate.java");

        elements.forEach(element -> {
            try {
                ClassDecorator decorator = element.getAnnotation(ClassDecorator.class);
                ClassDecoratorDriver driver = ClassUtils.instantiate(decorator.driver());
                info("Identified Class Decorator", driver);

                driver.compileInit(batch);

                DecoratorSpec spec = driver.decoratorSpec();

                String newAnnotation = TemplateEngine.bind(annotationTemplate,
                        "package", spec.getPackage(),
                        "name", spec.getName(),
                        "retention", spec.retentionPolicy().name(),
                        "annotation_body", Arrays.stream(spec.getProperties()).map(this::getBody)
                                .collect(Collectors.joining("\n")));

                batch.rawSource(spec.getPackage(), spec.getName(), newAnnotation);

                String processorName = spec.getName() + "$$AnnotationProcessor";
                String newProcessor = TemplateEngine.bind(processorTemplate,
                        "package", spec.getPackage(),
                        "name", processorName,
                        "annotation", spec.getPackage() + "." + spec.getName(),
                        "driver_package", driver.getClass().getPackage().getName(),
                        "driver", driver.getClass().getTypeName(),
                        "interfaces", Arrays.stream(driver.implementInterfaces())
                                .map(s -> "\"" + s + "\"").collect(Collectors.joining(",")),
                        "fields", Arrays.stream(driver.addFields())
                                .map(FieldDefinition::builderCode).collect(Collectors.joining(",")),
                        "methods", Arrays.stream(driver.addMethods())
                                .map(MethodDefinition::builderCode).collect(Collectors.joining(",")));

                batch.rawSource(spec.getPackage(), processorName, newProcessor);
            } catch (Exception e) {
                error("Exception caught handling class decorators!", e);
                warning("Attempting to continue...");
            }
        });
        return !elements.isEmpty();
    }

    private boolean handleMethodDecorators(JavaFileBatch batch, Set<? extends Element> elements) {
        String processorTemplate = ResourceUtils.readFile("blackhole/templates/MethodDecoratorProcessorTemplate.java");
        String annotationTemplate = ResourceUtils.readFile("blackhole/templates/MethodDecoratorTemplate.java");

        elements.forEach(element -> {
            try {
                MethodDecorator decorator = element.getAnnotation(MethodDecorator.class);
                MethodDecoratorDriver driver = ClassUtils.instantiate(decorator.driver());
                info("Identified Method Decorator", driver);

                driver.compileInit(batch);

                DecoratorSpec spec = driver.decoratorSpec();

                String newAnnotation = TemplateEngine.bind(annotationTemplate,
                        "package", spec.getPackage(),
                        "name", spec.getName(),
                        "retention", spec.retentionPolicy().name(),
                        "annotation_body", Arrays.stream(spec.getProperties()).map(this::getBody)
                                .collect(Collectors.joining("\n")));

                batch.rawSource(spec.getPackage(), spec.getName(), newAnnotation);

                String processorName = spec.getName() + "$$AnnotationProcessor";
                String newProcessor = TemplateEngine.bind(processorTemplate,
                        "package", spec.getPackage(),
                        "name", processorName,
                        "annotation", spec.getPackage() + "." + spec.getName(),
                        "driver_package", driver.getClass().getPackage().getName(),
                        "driver", driver.getClass().getTypeName(),
                        "fields", Arrays.stream(driver.addFields())
                                .map(FieldDefinition::builderCode).collect(Collectors.joining(",")));

                batch.rawSource(spec.getPackage(), processorName, newProcessor);
            } catch (Exception e) {
                error("Exception caught handling method decorators!", e);
                warning("Attempting to continue...");
            }
        });
        return !elements.isEmpty();
    }
}
