package plasma.blackhole.processor;

import plasma.blackhole.api.annotations.RequireNoArgConstructor;
import plasma.blackhole.util.ThrowableUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

//@WireService(Processor.class)
@RequireNoArgConstructor
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public abstract class AbstractBlackholeAnnotationProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    public AbstractBlackholeAnnotationProcessor() {} // Required

    public abstract Class<? extends Annotation>[] annotations();

    @Override
    public abstract boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv);

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Arrays.stream(annotations())
                .map(Class::getCanonicalName)
                .collect(Collectors.toSet());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    //TODO: Completion engine?
    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        return super.getCompletions(element, annotation, member, userText);
    }

    public Types getTypeUtils() {
        return typeUtils;
    }

    public Elements getElementUtils() {
        return elementUtils;
    }

    public Filer getFiler() {
        return filer;
    }

    public Messager getMessager() {
        return messager;
    }

    private void message(Kind k, Object... msg) {
        StringBuilder sb = new StringBuilder()
                .append("[Blackhole Processor][")
                .append(this.getClass().getCanonicalName())
                .append("] ");
        for (Object o : msg) {
            if (o instanceof Throwable) {
                sb.append(ThrowableUtils.toString((Throwable) o)).append('\n');
            } else {
                sb.append(o).append(' ');
            }
        }
        String s = sb.toString();
        messager.printMessage(k, s.substring(0, s.length()-1));
    }

    public void error(Object... msg) {
        message(Kind.ERROR, msg);
    }

    public void warning(Object... msg) {
        message(Kind.WARNING, msg);
    }

    public void mandatoryWarning(Object... msg) {
        message(Kind.MANDATORY_WARNING, msg);
    }

    public void info(Object... msg) {
        message(Kind.NOTE, msg);
    }

    public void other(Object... msg) {
        message(Kind.OTHER, msg);
    }
}
