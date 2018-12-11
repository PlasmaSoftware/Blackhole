package plasma.blackhole.processor;

import com.austinv11.servicer.WireService;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@WireService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BlackholeAnnotationProcessor extends AbstractProcessor {

    private final Set<String> ANNOTATIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(

    )));

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    public BlackholeAnnotationProcessor() {} // Required

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ANNOTATIONS;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return super.getCompletions(element, annotation, member, userText);
    }
}
