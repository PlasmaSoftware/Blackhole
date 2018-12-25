package plasma.blackhole.processor;

import com.squareup.javapoet.*;
import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.MethodDecoratorDriver;
import plasma.blackhole.api.annotations.ClassDecorator;
import plasma.blackhole.util.*;

import javax.annotation.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//TODO: Handle generics and varargs
public abstract class AbstractDecoratorImplProcessor extends AbstractBlackholeAnnotationProcessor {

    public abstract Target getTarget();

    public abstract Class<? extends ClassDecoratorDriver> classDriver();

    public abstract Class<? extends MethodDecoratorDriver> methodDriver();

    public abstract String pkg();

    public abstract String name();

    public abstract String[] interfaces(); //Used only for class decorators

    public abstract FieldDefinition[] fields();

    public abstract MethodDefinition[] methods();

    public Class<? extends Annotation> annotation() {
        return annotations()[0];
    }

    private boolean claimType(TypeElement te) { //Used to force a single decorator per class in each round
        //FIXME: use in-memory state somehow. Static fields?
        try {
            File tracker = new File("./blackhole_claims.tmp");
            if (!tracker.exists()) {
                if (!tracker.createNewFile())
                    throw new IOException("Cannot create blackhole_claims.tmp file!");
                tracker.deleteOnExit();
            }
            String name = te.getQualifiedName().toString();
            if (!Files.readAllLines(tracker.toPath()).contains(name)) {
                Files.write(tracker.toPath(), (name + "\n").getBytes(), StandardOpenOption.APPEND);
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Class toClass(TypeMirror type) {
        try {
            return Class.forName(type.toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Class toClass(VariableElement var) {
        return toClass(var.asType());
    }

    private String makeGetterTemplate(FieldDefinition var, boolean instance) {
//        try {
//            Class type = Class.forName(var.asType().toString());
//            if (type.isPrimitive()) {
//                return String.format("() -> (%s) $T.$L", );
//            } else {
                return instance ? "() -> $T.this.$L" : "() -> $T.$L";
//            }
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }

    private String makeSetterTemplate(FieldDefinition var, boolean instance) {
        if (java.lang.reflect.Modifier.isFinal(var.getModifiers())) {
            return "(o) -> {}";
        } else {
            return instance ? "(internal_binding) -> $T.this.$L = internal_binding" : "(internal_binding) -> $T.$L = internal_binding";
        }
    }

    private int computeModifiers(Element var) {
        int mod = 0;
        for (Modifier m : var.getModifiers()) {
            switch (m) {
                case PUBLIC:
                    mod |= java.lang.reflect.Modifier.PUBLIC;
                    break;
                case PROTECTED:
                    mod |= java.lang.reflect.Modifier.PROTECTED;
                    break;
                case PRIVATE:
                    mod |= java.lang.reflect.Modifier.PRIVATE;
                    break;
                case ABSTRACT:
                    mod |= java.lang.reflect.Modifier.ABSTRACT;
                    break;
                case DEFAULT:
                    break;
                case STATIC:
                    mod |= java.lang.reflect.Modifier.STATIC;
                    break;
                case FINAL:
                    mod |= java.lang.reflect.Modifier.FINAL;
                    break;
                case TRANSIENT:
                    mod |= java.lang.reflect.Modifier.TRANSIENT;
                    break;
                case VOLATILE:
                    mod |= java.lang.reflect.Modifier.VOLATILE;
                    break;
                case SYNCHRONIZED:
                    mod |= java.lang.reflect.Modifier.SYNCHRONIZED;
                    break;
                case NATIVE:
                    mod |= java.lang.reflect.Modifier.NATIVE;
                    break;
                case STRICTFP:
                    mod |= java.lang.reflect.Modifier.STRICT;
                    break;
            }
        }
        return mod;
    }

    private Modifier[] reflectModsToProcessorMods(int mod) {
        List<Modifier> mods = new ArrayList<>();
        if (java.lang.reflect.Modifier.isPublic(mod)) {
            mods.add(Modifier.PUBLIC);
        }
        if (java.lang.reflect.Modifier.isProtected(mod)) {
            mods.add(Modifier.PROTECTED);
        }
        if (java.lang.reflect.Modifier.isPrivate(mod)) {
            mods.add(Modifier.PRIVATE);
        }
        if (java.lang.reflect.Modifier.isAbstract(mod)) {
            mods.add(Modifier.ABSTRACT);
        }
        if (java.lang.reflect.Modifier.isStatic(mod)) {
            mods.add(Modifier.STATIC);
        }
        if (java.lang.reflect.Modifier.isFinal(mod)) {
            mods.add(Modifier.FINAL);
        }
        if (java.lang.reflect.Modifier.isTransient(mod)) {
            mods.add(Modifier.TRANSIENT);
        }
        if (java.lang.reflect.Modifier.isVolatile(mod)) {
            mods.add(Modifier.VOLATILE);
        }
        if (java.lang.reflect.Modifier.isSynchronized(mod)) {
            mods.add(Modifier.SYNCHRONIZED);
        }
        if (java.lang.reflect.Modifier.isNative(mod)) {
            mods.add(Modifier.NATIVE);
        }
        if (java.lang.reflect.Modifier.isStrict(mod)) {
            mods.add(Modifier.STRICTFP);
        }
        return mods.toArray(new Modifier[0]);
    }

    private String generateInvoker(MethodDefinition m) {
        String lambda = "$T.";
        if (!m.isStatic())
            lambda += "this.";
        List<String> argList = new ArrayList<>();
        for (int i = 0; i < m.getArgTypes().length; i++) {
            argList.add("(" + m.getArgTypes()[i].getCanonicalName() + ") args[" + i + "]");
        }
        String args = "(" + String.join(", ", argList) + ")";
        if (void.class.equals(m.getReturnType())) {
            return String.format("(args) -> {%s%s%s; return null;}", lambda, m.getName(), args);
        } else {
            return String.format("(args) -> {return (%s) %s%s%s;}", m.getReturnType().getCanonicalName(),
                    lambda, m.getName(), args);
        }
    }

    private MethodDefinition executableElementToMethodDef(ExecutableElement ee) {
        MethodDefinition.Builder builder = MethodDefinition.builder(ee.getSimpleName().toString());

        builder.addModifier(computeModifiers(ee))
                .setReturnType(toClass(ee.getReturnType()));

        Class<?>[] args = new Class[ee.getParameters().size()];
        for (int i = 0; i < args.length; i++) {
            args[i] = toClass(ee.getParameters().get(i));
        }
        builder.setArgTypes(args);

        AnnotationDefinition[] annotations = new AnnotationDefinition[ee.getAnnotationMirrors().size()];
        for (int i = 0; i < annotations.length; i++) {
            AnnotationMirror am = ee.getAnnotationMirrors().get(i);
            AnnotationDefinition.Builder ab = AnnotationDefinition.builder(toClass(am.getAnnotationType()));
            am.getElementValues().forEach((k, v) -> {
                String name = k.getSimpleName().toString();
                Object o = v.getValue();
                ab.bindParameter(name, o);
            });
            annotations[i] = ab.build();
        }
        builder.setAnnotations(annotations);

        return builder.build();
    }

    private FieldDefinition variableElementToFieldDef(VariableElement ve) {
        FieldDefinition.Builder builder = FieldDefinition.builder(ve.getSimpleName().toString(), toClass(ve));

        builder.addModifier(computeModifiers(ve));

        AnnotationDefinition[] annotations = new AnnotationDefinition[ve.getAnnotationMirrors().size()];
        for (int i = 0; i < annotations.length; i++) {
            AnnotationMirror am = ve.getAnnotationMirrors().get(i);
            AnnotationDefinition.Builder ab = AnnotationDefinition.builder(toClass(am.getAnnotationType()));
            am.getElementValues().forEach((k, v) -> {
                String name = k.getSimpleName().toString();
                Object o = v.getValue();
                ab.bindParameter(name, o);
            });
            annotations[i] = ab.build();
        }
        builder.setAnnotations(annotations);

        return builder.build();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        JavaFileBatch batch = new JavaFileBatch();
        TypeElement generated = getElementUtils().getTypeElement("javax.annotation.Generated");
        TypeElement myAnnotation = getElementUtils().getTypeElement(getSupportedAnnotationTypes().stream().findFirst().get());
        if (getTarget() == Target.TYPE) {
            Class<? extends ClassDecoratorDriver> driver = classDriver();

            roundEnv.getElementsAnnotatedWith(annotation()).forEach(e -> {
                TypeElement te = (TypeElement) e;
                if (te.getKind() == ElementKind.CLASS && claimType(te)) {
                    String qn = te.getQualifiedName().toString();
                    String newName = qn.substring(qn.lastIndexOf('.')+1) + "$$" + name() + "$Decorated";
                    String newPkg = qn.substring(0, qn.lastIndexOf('.'));
                    batch.newClass(newPkg, newName, b -> {
                        b.addAnnotation(AnnotationSpec.builder(Generated.class)
                                    .addMember("value", pkg() + "." + name())
                                    .build())
                                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

                        b.superclass(TypeName.get(te.asType()));

                        //Implement added interfaces
                        for (String i : interfaces()) {
                            b.addSuperinterface(ClassName.bestGuess(i));
                        }

                        //Initialize the driver
                        b.addField(FieldSpec
                                .builder(TypeName.get(ClassDecoratorDriver.class), "__DRIVER__",
                                        Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T();", driver))
                                .build());

                        //Force static init of superclass
                        b.addStaticBlock(CodeBlock.of("try { $T.forName($S); } catch ($T e) {}", Class.class, qn, Throwable.class));

                        //Transfer all annotations except for the current decorator and a potential @Generated annotation
                        te.getAnnotationMirrors().forEach(am -> {
                            TypeElement element = (TypeElement) am.getAnnotationType().asElement();
                            if (!element.equals(generated) && !element.equals(myAnnotation))
                                b.addAnnotation(AnnotationSpec.get(am));
                        });

                        //Collect things to be decorated

                        //First we need to collect ALL elements
                        List<ExecutableElement> constructors = te.getEnclosedElements()
                                .stream()
                                .filter(ee -> ee instanceof ExecutableElement)
                                .map(ExecutableElement.class::cast)
                                .filter(ee -> ee.getKind() == ElementKind.CONSTRUCTOR)
                                .collect(Collectors.toList());

                        List<ExecutableElement> methods = te.getEnclosedElements()
                                .stream()
                                .filter(ee -> ee instanceof ExecutableElement)
                                .map(ExecutableElement.class::cast)
                                .filter(ee -> ee.getKind() != ElementKind.CONSTRUCTOR)
                                .collect(Collectors.toList());

                        List<VariableElement> fields = te.getEnclosedElements()
                                .stream()
                                .filter(ee -> ee instanceof VariableElement)
                                .map(VariableElement.class::cast)
                                .filter(ee -> ee.getKind() == ElementKind.FIELD)
                                .collect(Collectors.toList());

                        List<Method> interfaceMethods = Arrays.stream(interfaces())
                                .map(s -> {
                                    try {
                                        return Class.forName(s);
                                    } catch (ClassNotFoundException e1) {
                                        throw new RuntimeException(e1);
                                    }
                                }).flatMap(c -> Arrays.stream(c.getMethods()))
                                .collect(Collectors.toList());

                        //Next we coerce them all into unique MethodDefinitions and FieldDefinitions
                        Set<MethodDefinition> staticMethodDefs = new HashSet<>();
                        methods.stream()
                                .filter(ee -> ee.getModifiers().contains(Modifier.STATIC))
                                .forEach(ee -> staticMethodDefs.add(executableElementToMethodDef(ee)));

                        Arrays.stream(methods())
                                .filter(MethodDefinition::isStatic)
                                .forEach(staticMethodDefs::add);


                        Set<MethodDefinition> instanceMethodDefs = new HashSet<>();
                        methods.stream()
                                .filter(ee -> !ee.getModifiers().contains(Modifier.STATIC))
                                .forEach(ee -> instanceMethodDefs.add(executableElementToMethodDef(ee)));

                        interfaceMethods.forEach(m -> instanceMethodDefs.add(MethodDefinition.from(m)));

                        Arrays.stream(methods())
                                .filter(md -> !md.isStatic())
                                .forEach(instanceMethodDefs::add);

                        Set<FieldDefinition> staticFieldDefs = new HashSet<>();
                        fields.stream()
                                .filter(f -> f.getModifiers().contains(Modifier.STATIC))
                                .forEach(f -> staticFieldDefs.add(variableElementToFieldDef(f)));

                        Arrays.stream(fields())
                                .filter(FieldDefinition::isStatic)
                                .forEach(staticFieldDefs::add);


                        Set<FieldDefinition> instanceFieldDefs = new HashSet<>();
                        fields.stream()
                                .filter(f -> !f.getModifiers().contains(Modifier.STATIC))
                                .forEach(f -> instanceFieldDefs.add(variableElementToFieldDef(f)));

                        Arrays.stream(fields())
                                .filter(fieldDefinition -> !fieldDefinition.isStatic())
                                .forEach(instanceFieldDefs::add);

                        //Proxy fields and bootstrap
                        b.addField(FieldSpec
                                .builder(TypeName.get(FieldProxy.class), "__STATIC_FIELD_PROXY__",
                                        Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T();", FieldProxy.class))
                                .build());

                        List<CodeBlock> staticFieldBindings = new ArrayList<>();
                        staticFieldDefs.stream()
                                .filter(FieldDefinition::isStatic)
                                .forEach(v -> {
                                    String name = v.getName();
                                    staticFieldBindings.add(
                                            CodeBlock.of(String.format("__STATIC_FIELD_PROXY__.bind($S, new $T(" +
                                                    "$S, $L, $L, $T.class, %s, %s);",
                                                        makeGetterTemplate(v, false), makeSetterTemplate(v, false)),
                                                    name,
                                                    true,
                                                    v.getModifiers(),
                                                    v.getType(),
                                                    qn, name,
                                                    qn, name));
                                });
                        b.addStaticBlock(CodeBlock.join(staticFieldBindings, "\n"));

                        b.addField(FieldSpec
                                .builder(TypeName.get(FieldProxy.class), "__INSTANCE_FIELD_PROXY__",
                                        Modifier.FINAL, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T(__STATIC_FIELD_PROXY__);", FieldProxy.class))
                                .build());

                        List<CodeBlock> instanceFieldBindings = new ArrayList<>();
                        instanceFieldDefs.stream()
                                .filter(v -> !v.isStatic())
                                .forEach(v -> {
                                    String name = v.getName();
                                    instanceFieldBindings.add(
                                            CodeBlock.of(String.format("__INSTANCE_FIELD_PROXY__.bind($S, new $T(" +
                                                            "$S, $L, $L, $T.class, %s, %s);",
                                                    makeGetterTemplate(v, true), makeSetterTemplate(v, true)),
                                                    name,
                                                    false,
                                                    v.getModifiers(),
                                                    v.getType(),
                                                    qn, name,
                                                    qn, name));
                                });
                        b.addMethod(MethodSpec.methodBuilder("__FIELD_PROXY_INIT__")
                                .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                                .addCode(CodeBlock.join(instanceFieldBindings, "\n"))
                                .build());

                        //Proxy methods and bootstrap
                        b.addField(FieldSpec
                                .builder(TypeName.get(FieldProxy.class), "__STATIC_METHOD_PROXY__",
                                        Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T();", MethodProxy.class))
                                .build());

                        List<CodeBlock> staticMethodBindings = new ArrayList<>();
                        staticMethodDefs.stream()
                                .filter(MethodDefinition::isStatic)
                                .forEach(m -> {
                                    String name = m.getName();
                                    List<String> args = Arrays.stream(m.getArgTypes())
                                            .map(p -> p.getCanonicalName() + ".class")
                                            .collect(Collectors.toList());
                                    staticMethodBindings.add(
                                            CodeBlock.of("__STATIC_METHOD_PROXY__.bind($T.from($S, new Class[]{$L}), new $T(" +
                                                            "$S, $L, $T.class, new Class[]{$L}, $L));",
                                                    MethodIdentifier.class,
                                                    name,
                                                    String.join(", ", args),
                                                    MethodBinding.class,
                                                    name,
                                                    m.getModifiers(),
                                                    m.getReturnType(),
                                                    String.join(", ", args),
                                                    generateInvoker(m)));
                                });
                        b.addStaticBlock(CodeBlock.join(staticMethodBindings, "\n"));

                        b.addField(FieldSpec
                                .builder(TypeName.get(FieldProxy.class), "__INSTANCE_METHOD_PROXY__",
                                        Modifier.FINAL, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T(__STATIC_METHOD_PROXY__);", FieldProxy.class))
                                .build());

                        List<CodeBlock> instanceMethodBindings = new ArrayList<>();
                        instanceMethodDefs.stream()
                                .filter(m -> !m.isStatic())
                                .forEach(m -> {
                                    String name = m.getName();
                                    List<String> args = Arrays.stream(m.getArgTypes())
                                            .map(p -> p.getCanonicalName() + ".class")
                                            .collect(Collectors.toList());
                                    instanceMethodBindings.add(
                                            CodeBlock.of("__INSTANCE_METHOD_PROXY__.bind($T.from($S, new Class[]{$L}), new $T(" +
                                                            "$S, $L, $T.class, new Class[]{$L}, $L));",
                                                    MethodIdentifier.class,
                                                    name,
                                                    String.join(", ", args),
                                                    MethodBinding.class,
                                                    name,
                                                    m.getModifiers(),
                                                    m.getReturnType(),
                                                    String.join(", ", args),
                                                    generateInvoker(m)));
                                });
                        b.addMethod(MethodSpec.methodBuilder("__METHOD_PROXY_INIT__")
                                .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                                .addCode(CodeBlock.join(instanceMethodBindings, "\n"))
                                .build());

                        b.addStaticBlock(CodeBlock.of("__DRIVER__.cinit(__STATIC_FIELD_PROXY__);"));

                        constructors.forEach(con -> {
                            MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                                    .addModifiers(Modifier.PUBLIC);

                            List<? extends VariableElement> parameters = con.getParameters();
                            for (int i = 0; i < parameters.size(); i++) {
                                VariableElement param = parameters.get(i);
                                TypeName type = TypeName.get(param.asType());
                                String name = "arg" + i;
                                builder.addParameter(ParameterSpec.builder(type, name)
                                        .addModifiers(param.getModifiers())
                                        .build());
                            }

                            String argListString = IntStream.range(0, parameters.size()).mapToObj(i -> "arg" + i)
                                    .collect(Collectors.joining(","));

                            builder.addStatement("super(" + argListString + ");");

                            builder.addStatement("this.__FIELD_PROXY_INIT__();");
                            builder.addStatement("this.__METHOD_PROXY_INIT__();");

                            builder.addStatement("__DRIVER__.init(this.__INSTANCE_FIELD_PROXY__, " +
                                    "this.__INSTANCE_METHOD_PROXY__$L);", argListString.isEmpty() ? "" : ", " + argListString);

                            b.addMethod(builder.build());
                        });

                        //Only new fields need to be redefined
                        Arrays.stream(fields()).forEach(fd -> {
                            FieldSpec.Builder builder = FieldSpec.builder(fd.getType(), fd.getName(),
                                    reflectModsToProcessorMods(fd.getModifiers()));

                            for (AnnotationDefinition ad : fd.getAnnotations()) {
                                AnnotationSpec.Builder aBuilder = AnnotationSpec.builder(ad.getAnnotation());
                                ad.getBindings().forEach((k, v) -> {
                                    aBuilder.addMember(k, "$L", AnnotationDefinition.toAnnotationLiteral(v));
                                });
                                builder.addAnnotation(aBuilder.build());
                            }

                            b.addField(builder.build());
                        });

                        Stream.concat(staticMethodDefs.stream(), instanceMethodDefs.stream()).forEach(md -> {
                            MethodSpec.Builder builder = MethodSpec.methodBuilder(md.getName())
                                    .addModifiers(reflectModsToProcessorMods(md.getModifiers()))
                                    .returns(md.getReturnType());

                            for (int i = 0; i < md.getArgTypes().length; i++) {
                                Class<?> param = md.getArgTypes()[i];
                                builder.addParameter(ParameterSpec.builder(param, "arg" + i).build());
                            }

                            boolean isStatic = java.lang.reflect.Modifier.isStatic(md.getModifiers());
                            int argLen = md.getArgTypes().length;
                            String stmt = String.format("__%1$s_FIELD_PROXY__, __%1$s__METHOD_PROXY__, __%1$s__METHOD_PROXY__",
                                    isStatic ? "STATIC" : "INSTANCE");
                            String temp = md.getReturnType().equals(void.class) ? "" : "return ";

                            builder.addStatement(temp + "__DRIVER__.methodWrap($L.getBinding($T.from($S, new Class[]{$L})$L);",
                                    stmt, MethodIdentifier.class, md.getName(),
                                    Arrays.stream(md.getArgTypes()).map(c -> c.getCanonicalName() + ".class")
                                            .collect(Collectors.joining(",")),
                                    argLen == 0 ? "" : "," + IntStream.range(0, argLen).mapToObj(i -> "arg" + i)
                                            .collect(Collectors.joining(",")));

                            for (AnnotationDefinition ad : md.getAnnotations()) {
                                AnnotationSpec.Builder aBuilder = AnnotationSpec.builder(ad.getAnnotation());
                                ad.getBindings().forEach((k, v) -> {
                                    aBuilder.addMember(k, "$L", AnnotationDefinition.toAnnotationLiteral(v));
                                });
                                builder.addAnnotation(aBuilder.build());
                            }

                            b.addMethod(builder.build());
                        });

                        return b.build();
                    });
                }
            });
        } else {
            Class<? extends MethodDecoratorDriver> driver = methodDriver();
            //TODO
        }

        try {
            batch.publish(getFiler());
        } catch (IOException e) {
            error("Exception caught running generated decorator processor!", e);
        }

        return true;
    }

    public enum Target {
        METHOD, TYPE
    }
}
