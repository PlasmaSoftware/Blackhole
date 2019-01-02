package plasma.blackhole.test;

import com.google.common.base.Joiner;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Test;
import plasma.blackhole.processor.MakeAnnotationAnnotationProcessor;

import javax.tools.StandardLocation;
import java.util.Arrays;
import java.util.Collections;

import static com.google.common.truth.Truth.assert_;

public class TestMakeAnnotation {

    public CompileTester checkFile(String src) {
        return assert_().about(JavaSourcesSubjectFactory.javaSources())
                .that(Collections.singletonList(JavaFileObjects.forSourceString("plasma.blackhole.test.Temp", src)))
                .processedWith(new MakeAnnotationAnnotationProcessor());
    }

    @Test
    public void testDecoratorGeneration() {
        checkFile(Joiner.on("\n").join(
                "package plasma.blackhole.test;",
                "import plasma.blackhole.api.annotations.MakeAnnotation;",
                "import plasma.blackhole.api.ClassDecoratorDriver;",
                "import plasma.blackhole.util.*;",
                "@MakeAnnotation(\"TempDecorator\")",
                "public class Temp extends ClassDecoratorDriver {",
                "public void runtimeInit(AnnotationDefinition decorator, Class<?> clazz, FieldProxy proxy) {}",
                "public void init(AnnotationDefinition decorator, Class<?> clazz, FieldProxy fieldProxy, MethodProxy methodProxy, Object... args) {}",
                "public Object methodWrap(AnnotationDefinition decorator, Class<?> clazz, FieldProxy fieldProxy, MethodProxy methodProxy, MethodBinding original, Object... args) {return null;}",
                "}"
        )).compilesWithoutError();
    }
}
