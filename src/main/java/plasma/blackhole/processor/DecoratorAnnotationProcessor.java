package plasma.blackhole.processor;

import com.austinv11.servicer.WireService;
import plasma.blackhole.api.CompileTimeHook;
import plasma.blackhole.api.NoOpCompileTimeHook;
import plasma.blackhole.api.annotations.ClassDecorator;
import plasma.blackhole.api.annotations.MethodDecorator;
import plasma.blackhole.util.*;
import plasma.blackhole.util.internal.ClassUtils;
import plasma.blackhole.util.internal.ResourceUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
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
        try {
            JavaFileBatch batch = new JavaFileBatch();
            boolean changed = handleClassDecorators(roundEnv, batch, roundEnv.getElementsAnnotatedWith(ClassDecorator.class));
            changed = changed | handleMethodDecorators(roundEnv, batch, roundEnv.getElementsAnnotatedWith(MethodDecorator.class));
            try {
                batch.publish(getFiler());
            } catch (IOException e) {
                error(e);
                return false;
            }
            return changed;
        } catch (Throwable t) {
            error(t);
        }
        return false;
    }

    private boolean handleClassDecorators(RoundEnvironment roundEnv, JavaFileBatch batch, Set<? extends Element> elements) {
        String processorTemplate = ResourceUtils.readFileOrEmpty("blackhole/templates/DecoratorProcessorTemplate.java");

        elements.forEach(element -> {
            try {
                Target t = element.getAnnotation(Target.class);
                if (t == null || t.value().length > 2 || t.value().length == 0
                        || !Arrays.stream(t.value()).allMatch(e -> e == ElementType.TYPE || e == ElementType.METHOD))
                    throw new AssertionError("Decorators must only have TYPE and/or METHOD targets set!");

                ClassDecorator decorator = element.getAnnotation(ClassDecorator.class);
                TypeMirror compileDriver = annotationHack(decorator::onCompile);
                CompileTimeHook hook = ((TypeElement) getTypeUtils().asElement(compileDriver))
                        .getQualifiedName().contentEquals(NoOpCompileTimeHook.class.getCanonicalName())
                        ? new NoOpCompileTimeHook()
                        : ClassUtils.instantiate(mirroredTypeWorkaround(roundEnv, decorator::onCompile));
                TypeMirror driver = annotationHack(decorator::value);
                String fqn = ((TypeElement) getTypeUtils().asElement(driver)).getQualifiedName().toString();
                info("Identified Class Decorator", fqn);

                hook.compileInit(batch);

                String pkg = fqn.substring(0, fqn.lastIndexOf('.'));
                String name = fqn.substring(fqn.lastIndexOf('.')+1);
                String processorName = name + "$$AnnotationProcessor";
                String annotation = ((TypeElement) element).getQualifiedName().toString();
                String newProcessor = TemplateEngine.bind(processorTemplate,
                        "package", pkg,
                        "name", processorName,
                        "annotation", annotation,
                        "annotation_name", annotation.substring(annotation.lastIndexOf('.')+1),
                        "driver_package", pkg,
                        "driver", name,
                        "target", "TYPE",
                        "class_driver", name + ".class",
                        "method_driver", "null",
                        "interfaces", Arrays.stream(hook.implementInterfaces())
                                .map(s -> "\"" + s + "\"").collect(Collectors.joining(",")),
                        "fields", Arrays.stream(hook.addFields())
                                .map(FieldDefinition::builderCode).collect(Collectors.joining(",")),
                        "methods", Arrays.stream(hook.addMethods())
                                .map(MethodDefinition::builderCode).collect(Collectors.joining(",")));

                batch.rawSource(pkg, processorName, newProcessor);
            } catch (Exception e) {
                error("Exception caught handling class decorators!", e);
                warning("Attempting to continue...");
            }
        });
        return !elements.isEmpty();
    }

    private boolean handleMethodDecorators(RoundEnvironment roundEnv, JavaFileBatch batch, Set<? extends Element> elements) {
        String processorTemplate = ResourceUtils.readFileOrEmpty("blackhole/templates/DecoratorProcessorTemplate.java");

        elements.forEach(element -> {
            try {
                Target t = element.getAnnotation(Target.class);
                if (t == null || t.value().length > 2 || t.value().length == 0
                        || !Arrays.stream(t.value()).allMatch(e -> e == ElementType.TYPE || e == ElementType.METHOD))
                    throw new AssertionError("Decorators must only have TYPE and/or METHOD targets set!");

                MethodDecorator decorator = element.getAnnotation(MethodDecorator.class);
                TypeMirror compileDriver = annotationHack(decorator::onCompile);
                CompileTimeHook hook = ((TypeElement) getTypeUtils().asElement(compileDriver))
                        .getQualifiedName().contentEquals(NoOpCompileTimeHook.class.getCanonicalName())
                        ? new NoOpCompileTimeHook()
                        : ClassUtils.instantiate(mirroredTypeWorkaround(roundEnv, decorator::onCompile));
                TypeMirror driver = annotationHack(decorator::value);
                String fqn = ((TypeElement) getTypeUtils().asElement(driver)).getQualifiedName().toString();
                info("Identified Method Decorator", fqn);

                hook.compileInit(batch);

                String pkg = fqn.substring(0, fqn.lastIndexOf('.'));
                String name = fqn.substring(fqn.lastIndexOf('.')+1);
                String processorName = name + "$$AnnotationProcessor";
                String annotation = ((TypeElement) element).getQualifiedName().toString();
                String newProcessor = TemplateEngine.bind(processorTemplate,
                        "package", pkg,
                        "name", processorName,
                        "annotation", annotation,
                        "annotation_name", annotation.substring(annotation.lastIndexOf('.')+1),
                        "driver_package", pkg,
                        "driver", name,
                        "target", "METHOD",
                        "class_driver", "null",
                        "method_driver", name + ".class",
                        "interfaces", Arrays.stream(hook.implementInterfaces())
                                .map(s -> "\"" + s + "\"").collect(Collectors.joining(",")),
                        "fields", Arrays.stream(hook.addFields())
                                .map(FieldDefinition::builderCode).collect(Collectors.joining(",")),
                        "methods", Arrays.stream(hook.addMethods())
                                .map(MethodDefinition::builderCode).collect(Collectors.joining(",")));

                batch.rawSource(pkg, processorName, newProcessor);
            } catch (Exception e) {
                error("Exception caught handling method decorators!", e);
                warning("Attempting to continue...");
            }
        });
        return !elements.isEmpty();
    }
}
