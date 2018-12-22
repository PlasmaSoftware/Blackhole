package plasma.blackhole.test;

import com.google.common.base.Joiner;
import com.google.testing.compile.CompileTester;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.Test;
import plasma.blackhole.processor.RequireNoArgConstructorAnnotationProcessor;

import java.util.Arrays;

import static com.google.common.truth.Truth.assert_;

public class TestRequireNoArgConstructor {

    public CompileTester checkFile(String src) {
        return assert_().about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(JavaFileObjects.forSourceString("plasma.blackhole.test.Temp", src),
                        JavaFileObjects.forSourceLines("plasma.blackhole.test.IFace",
                                "package plasma.blackhole.test;",
                                "import plasma.blackhole.api.annotations.RequireNoArgConstructor;",
                                "@RequireNoArgConstructor",
                                "public abstract class IFace {}"
                        )))
                .processedWith(new RequireNoArgConstructorAnnotationProcessor());
    }

    @Test
    public void testImplicitConstructor() {
        checkFile(Joiner.on("\n").join(
                "package plasma.blackhole.test;",
                "public class Temp extends IFace {}"
        )).compilesWithoutWarnings();
    }

    @Test
    public void testExplicitConstructor() {
        checkFile(Joiner.on("\n").join(
                "package plasma.blackhole.test;",
                "public class Temp extends IFace {",
                "public Temp() {}",
                "}"
        )).compilesWithoutWarnings();
    }

    @Test
    public void testExplicitConstructors() {
        checkFile(Joiner.on("\n").join(
                "package plasma.blackhole.test;",
                "public class Temp extends IFace {",
                "public Temp() {}",
                "public Temp(String s) {}",
                "}"
        )).compilesWithoutWarnings();
    }

    @Test(expected = RuntimeException.class)
    public void testVisibilityCheck() {
        checkFile(Joiner.on("\n").join(
                "package plasma.blackhole.test;",
                "public class Temp extends IFace {",
                "private Temp() {}",
                "}"
        )).failsToCompile();
    }

    @Test(expected = RuntimeException.class)
    public void testNoCorrectConstructor() {
        checkFile(Joiner.on("\n").join(
                "package plasma.blackhole.test;",
                "public class Temp extends IFace {",
                "public Temp(String s) {}",
                "}"
        )).failsToCompile();
    }
}
