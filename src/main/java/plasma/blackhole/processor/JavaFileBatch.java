package plasma.blackhole.processor;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class JavaFileBatch {

    private final List<JavaFile> files = new ArrayList<>();

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

    private void batchSpec(String pkg, String name, TypeSpec spec) {
        JavaFile file = JavaFile.builder(pkg, spec)
                .addFileComment("This is an auto-generated file by Blackhole, do not edit it!")
                .build();
        files.add(file);
    }

    public void publish(Filer filer) throws IOException {
        for (JavaFile jf : files) {
            jf.writeTo(filer);
        }
    }
}
