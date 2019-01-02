package plasma.blackhole.processor;

import com.austinv11.servicer.WireService;
import com.squareup.javapoet.AnnotationSpec;
import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.annotations.ClassDecorator;
import plasma.blackhole.api.annotations.MakeAnnotation;
import plasma.blackhole.api.annotations.MethodDecorator;

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.annotation.*;
import java.util.Set;

@WireService(Processor.class)
public class MakeAnnotationAnnotationProcessor extends AbstractBlackholeAnnotationProcessor {

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{MakeAnnotation.class};
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        JavaFileBatch jfb = new JavaFileBatch();
        //TODO: Sanity checks?
        roundEnv.getElementsAnnotatedWith(MakeAnnotation.class).forEach(e -> {
            TypeElement te = (TypeElement) e;
            String fqn = te.getQualifiedName().toString();
            String pkg = fqn.substring(0, fqn.lastIndexOf('.'));
            String name = te.getAnnotation(MakeAnnotation.class).value();
            boolean isClassDriver = getTypeUtils().isAssignable(te.asType(),
                    getElementUtils().getTypeElement(ClassDecoratorDriver.class.getCanonicalName()).asType());
            jfb.newAnnotation(pkg, name, ab -> {
                ab.addAnnotation(AnnotationSpec.builder(Generated.class)
                        .addMember("value", "$S", this.getClass().getCanonicalName()).build());
                ab.addAnnotation(AnnotationSpec.builder(isClassDriver ? ClassDecorator.class : MethodDecorator.class)
                        .addMember("value", "$L.class", fqn).build());
                ab.addAnnotation(AnnotationSpec.builder(Retention.class)
                        .addMember("value", "$L.SOURCE", RetentionPolicy.class.getCanonicalName()).build());
                ab.addAnnotation(AnnotationSpec.builder(Target.class)
                        .addMember("value", "$L.$L", ElementType.class.getCanonicalName(), isClassDriver ? "TYPE" : "METHOD").build());
                return ab.build();
            });
        });

        try {
            jfb.publish(getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
