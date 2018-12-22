package plasma.blackhole.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JavaFileBatch {

    private final List<Writable> files = new ArrayList<>();

    public void newClass(String pkg, String name, Function<TypeSpec.Builder, TypeSpec> callback) {
        TypeSpec spec = callback.apply(TypeSpec.classBuilder(name));
        batchSpec(pkg, name, spec);
    }

    public void newAnnotation(String pkg, String name, Function<TypeSpec.Builder, TypeSpec> callback) {
        TypeSpec spec = callback.apply(TypeSpec.annotationBuilder(name));
        batchSpec(pkg, name, spec);
    }

    public void newEnum(String pkg, String name, Function<TypeSpec.Builder, TypeSpec> callback) {
        TypeSpec spec = callback.apply(TypeSpec.enumBuilder(name));
        batchSpec(pkg, name, spec);
    }

    public void newInterface(String pkg, String name, Function<TypeSpec.Builder, TypeSpec> callback) {
        TypeSpec spec = callback.apply(TypeSpec.interfaceBuilder(name));
        batchSpec(pkg, name, spec);
    }

    public void rawSource(String pkg, String name, String src) {
        files.add(new SourceWrapper(pkg, name, src));
    }

    private void batchSpec(String pkg, String name, TypeSpec spec) {
        JavaFile file = JavaFile.builder(pkg, spec)
                .addFileComment("This is an auto-generated file by Blackhole, do not edit it!")
                .build();
        files.add(new JavaFileWrapper(file));
    }

    public void publish(Filer filer) throws IOException {
        for (Writable w : files) {
            w.writeTo(filer);
        }
    }

    private interface Writable {

        void writeTo(Filer f) throws IOException;
    }

    public class JavaFileWrapper implements Writable {

        private final JavaFile jf;

        public JavaFileWrapper(JavaFile jf) {
            this.jf = jf;
        }

        @Override
        public void writeTo(Filer f) throws IOException {
            jf.writeTo(f);
        }
    }

    public class SourceWrapper implements Writable {

        private final String pkg;
        private final String name;
        private final String src;

        public SourceWrapper(String pkg, String name, String src) {
            this.pkg = pkg;
            this.name = name;
            this.src = src;
        }

        @Override
        public void writeTo(Filer f) throws IOException {
            JavaFileObject jfo = f.createSourceFile(pkg + "." + name);
            try (Writer w = jfo.openWriter()) {
                w.write(src);
            } catch (Exception e) {
                jfo.delete();
                throw e;
            }
        }
    }
}
