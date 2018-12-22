package plasma.blackhole.test;

import com.google.common.base.Joiner;
import org.junit.Test;
import plasma.blackhole.util.TemplateEngine;

import static org.junit.Assert.assertEquals;

public class TestTemplateEngine {

    private String noReplacement = Joiner.on('\n').join(
            "package hello;",
            "public class thing {",
            "thing() {}",
            "}"
    );

    private String replacement = Joiner.on('\n').join(
            "package ${package};",
            "public class thing {",
            "thing() {}",
            "}"
    );

    private String doubleReplacement = Joiner.on('\n').join(
            "package hello;",
            "public class ${name} {",
            "${name}() {}",
            "}"
    );

    @Test
    public void testNoReplacement() {
        assertEquals(noReplacement, TemplateEngine.bind(noReplacement,
                "package", "hello",
                "name", "thing"));
    }

    @Test
    public void testReplacement() {
        assertEquals(noReplacement, TemplateEngine.bind(replacement,
                "package", "hello",
                "name", "thing"));
    }

    @Test
    public void testDoubleReplacement() {
        assertEquals(noReplacement, TemplateEngine.bind(doubleReplacement,
                "package", "hello",
                "name", "thing"));
    }
}
