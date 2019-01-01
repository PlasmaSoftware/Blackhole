package plasma.blackhole.util.internal;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class CompilerUtils {

    private static Set<String> getResolutionList(RoundEnvironment roundEnv) {
        ImportResolver resolver = new ImportResolver();
        resolver.scan(roundEnv.getRootElements(), null);
        return resolver.getImportedTypes();
    }

    private static boolean isLoaded(String fqn) {
        try {
            Class.forName(fqn);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String getSource(Filer filer, String fqn) {
        String source = ResourceUtils.readFile(filer, fqn.replace('.', '/'), StandardLocation.SOURCE_PATH);
//        String root = filer.getResource(StandardLocation.SOURCE_PATH, "", "").getName();
        return source;
    }

    public static synchronized <T> Class<T> compile(RoundEnvironment roundEnv, Filer filer, Types typeUtils, TypeMirror typeMirror) {
        try {
            String fqn = ((TypeElement) typeUtils.asElement(typeMirror)).getQualifiedName().toString();

            Set<String> toResolve = getResolutionList(roundEnv);
            toResolve.stream().filter(n -> !isLoaded(n)).forEach(n -> {
                try {
                    naiveCompile(n, getSource(filer, n));
                } catch (Exception e) {
                    compile(roundEnv, filer, typeUtils,
                            roundEnv.getRootElements()
                                    .stream()
                                    .filter(te -> te instanceof TypeElement)
                                    .map(Element::asType)
                                    .findFirst().get());
                }
            });
            return naiveCompile(fqn, getSource(filer, fqn));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> Class<T> naiveCompile(String fqn, String source) {
        try {
            return (Class<T>) net.openhft.compiler.CompilerUtils.CACHED_COMPILER.loadFromJava(fqn, source);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
