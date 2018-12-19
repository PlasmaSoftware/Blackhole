package plasma.blackhole.processor;

import com.austinv11.servicer.WireService;
import plasma.blackhole.api.annotations.RequireNoArgConstructor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@WireService(Processor.class)
public class RequireNoArgConstructorAnnotationProcessor extends AbstractBlackholeAnnotationProcessor {

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{RequireNoArgConstructor.class};
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(RequireNoArgConstructor.class).forEach(e -> {
            if (e.getKind() == ElementKind.CLASS && !e.getModifiers().contains(Modifier.ABSTRACT)) {
                if (!e.getModifiers().contains(Modifier.PUBLIC))
                    throw new AssertionError("Implementations of classes with " +
                            "@RequireNoArgConstructor must be public!");

                TypeElement te = (TypeElement) e;
                List<ExecutableElement> constructors = te.getEnclosedElements().stream()
                        .filter(ee -> (ee.getKind() == ElementKind.CONSTRUCTOR))
                        .map(ee -> (ExecutableElement) ee)
                        .collect(Collectors.toList());
                if (constructors.size() != 0) {
                    Optional<ExecutableElement> defaultConstructor = constructors.stream()
                            .filter(ee -> ee.getParameters().isEmpty()).findFirst();

                    if (!defaultConstructor.isPresent()
                            || !defaultConstructor.get().getModifiers().contains(Modifier.PUBLIC))
                        throw new AssertionError("Implementations of classes with " +
                                "@RequireNoArgConstructor must have a public no-arg constructor!");
                }
            }
        });
        return false;  //We don't change anything, only inspect
    }
}
