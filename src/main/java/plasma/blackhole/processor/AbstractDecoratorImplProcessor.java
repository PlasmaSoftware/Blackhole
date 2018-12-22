package plasma.blackhole.processor;

import com.squareup.javapoet.AnnotationSpec;
import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.MethodDecoratorDriver;

import javax.annotation.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

public abstract class AbstractDecoratorImplProcessor extends AbstractBlackholeAnnotationProcessor {

    public abstract Target getTarget();

    public abstract Class<? extends ClassDecoratorDriver> classDriver();

    public abstract Class<? extends MethodDecoratorDriver> methodDriver();

    public abstract String pkg();

    public abstract String name();

    public Class<? extends Annotation> annotation() {
        return annotations()[0];
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        JavaFileBatch batch = new JavaFileBatch();
        if (getTarget() == Target.TYPE) {
            Class<? extends ClassDecoratorDriver> driver = classDriver();

            roundEnv.getElementsAnnotatedWith(annotation()).forEach(e -> {
                TypeElement te = (TypeElement) e;
                if (te.getKind() == ElementKind.CLASS) {
                    String newName = te.getSimpleName().toString() + "$$" + name() + "$$Decorated";
                    batch.newClass(pkg(), newName, b -> {
                        b.addAnnotation(AnnotationSpec.builder(Generated.class)
                                    .addMember("value", pkg() + "." + name())
                                    .build())
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

                        te.getAnnotationMirrors().forEach(am -> {
                            String name = am
                        });

                        return b.build();
                    });
                }
            });
        } else {
            Class<? extends MethodDecoratorDriver> driver = methodDriver();
            //TODO
        }

        try {
            batch.publish(getFiler());
        } catch (IOException e) {
            error("Exception caught running generated decorator processor!", e);
        }

        return true;
    }

    public enum Target {
        METHOD, TYPE
    }
}
