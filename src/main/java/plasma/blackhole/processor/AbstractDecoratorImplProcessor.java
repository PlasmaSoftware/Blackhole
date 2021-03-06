package plasma.blackhole.processor;

import com.squareup.javapoet.*;
import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.MethodDecoratorDriver;
import plasma.blackhole.api.annotations.Decorated;
import plasma.blackhole.util.*;
import plasma.blackhole.util.internal.ClassTypeProxy;
import plasma.blackhole.util.internal.ClassUtils;
import plasma.blackhole.util.internal.ResourceUtils;

import javax.annotation.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//TODO: Handle generics and varargs
public abstract class AbstractDecoratorImplProcessor extends AbstractBlackholeAnnotationProcessor {

    private static final long runid = System.currentTimeMillis();

    private TypeElement generated;
    private TypeElement myAnnotation;
    private TypeElement decorated;

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
            File tracker = new File("./blackhole_claims.tmp").toPath().toAbsolutePath().toFile();
            if (tracker.exists()) {
                long lastRunid = Long.valueOf(Files.readAllLines(tracker.toPath()).get(0));
                if (lastRunid != runid)
                    if (!tracker.delete())
                        throw new RuntimeException("Cannot clean old claim file! Is another build running? Location: " + tracker.getPath());
            }
            if (!tracker.exists()) {
                if (!tracker.createNewFile())
                    throw new IOException("Cannot create blackhole_claims.tmp file!");

                Files.write(tracker.toPath(), (runid + "\n").getBytes(), StandardOpenOption.APPEND);
                tracker.deleteOnExit();
            }
            String name = te.getQualifiedName().toString() + "!";
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
        return ClassUtils.stringToClass(type.toString());
    }

    private Class toClass(VariableElement var) {
        return toClass(var.asType());
    }

    private String escapeQnForCodeBlock(String fqn) {
        return fqn.replace("$", "$$");
    }

    private String makeGetterTemplate(FieldDefinition var, boolean instance, String parent) {
//        try {
//            Class type = Class.forName(var.asType().toString());
//            if (type.isPrimitive()) {
//                return String.format("() -> (%s) $T.$L", );
//            } else {
                return instance ? "() -> this.$L" : "() -> " + escapeQnForCodeBlock(parent) + ".$L";
//            }
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }

    private String makeSetterTemplate(FieldDefinition var, boolean instance, String parent, String name) {
        if (java.lang.reflect.Modifier.isFinal(var.getModifiers())) {
            return "(o) -> {}";
        } else {
            return instance ? "(internal_binding) -> this." + name + " = internal_binding" : "(internal_binding) -> " + escapeQnForCodeBlock(parent) + "." + name +" = internal_binding";
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

    private String generateInvoker(String originatingFqn, MethodDefinition m) {
        String lambda;
        if (!m.isStatic())
            lambda = "super.";
        else {
            lambda = escapeQnForCodeBlock(originatingFqn) + ".";
        }
        List<String> argList = new ArrayList<>();
        for (int i = 0; i < m.getArgTypes().length; i++) {
            argList.add("(" + escapeQnForCodeBlock(m.getArgTypes()[i].getCanonicalName()) + ") args[" + i + "]");
        }
        String args = "(" + String.join(", ", argList) + ")";
        if (void.class.equals(m.getReturnType())) {
            return String.format("(args) -> {%s%s%s; return null;}", lambda, m.getName(), args);
        } else {
            return String.format("(args) -> {return (%s) %s%s%s;}", escapeQnForCodeBlock(m.getReturnType().getCanonicalName()),
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

        //Remove decorator annotations
        int sizeReduction = 0;
        AnnotationDefinition[] annotations = new AnnotationDefinition[ee.getAnnotationMirrors().size()];
        for (int i = 0; i < annotations.length; i++) {
            AnnotationMirror am = ee.getAnnotationMirrors().get(i);
            TypeElement element = (TypeElement) am.getAnnotationType().asElement();
            if (element.equals(generated) || element.equals(myAnnotation) || element.equals(decorated)) {
                sizeReduction++;
                continue;
            }

            AnnotationDefinition.Builder ab = AnnotationDefinition.builder(toClass(am.getAnnotationType()));
            am.getElementValues().forEach((k, v) -> {
                String name = k.getSimpleName().toString();
                Object o = v.getValue();
                if (o instanceof TypeMirror)
                    o = new ClassTypeProxy((TypeMirror) o);
                ab.bindParameter(name, o);
            });
            annotations[i-sizeReduction] = ab.build();
        }
        builder.setAnnotations(Arrays.copyOf(annotations, annotations.length - sizeReduction));

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
                if (o instanceof TypeMirror)
                    o = new ClassTypeProxy((TypeMirror) o);
                ab.bindParameter(name, o);
            });
            annotations[i] = ab.build();
        }
        builder.setAnnotations(annotations);

        return builder.build();
    }

    private OutputStream hackStream(JavaFileManager.Location location, String path) throws IOException {
        String finalPath = getFiler().getResource(location, "", path).toUri().getPath().substring(1);
        if (finalPath.startsWith("/") || finalPath.startsWith("\\")) //Not sure why, but this occasionally occurs
            finalPath = finalPath.substring(1);
        File file = new File(finalPath);
        if (!file.exists()) {
//            error(finalPath);
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return new FileOutputStream(file);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            generated = getElementUtils().getTypeElement("javax.annotation.Generated");
            myAnnotation = getElementUtils().getTypeElement(getSupportedAnnotationTypes().stream().findFirst().get());
            decorated = getElementUtils().getTypeElement("plasma.blackhole.api.annotations.Decorated");

            Indexer<String> index = Indexer.readStringIndex(ResourceUtils.readFileOrEmpty(getFiler(), "blackhole/decorated.idx", Charset.forName("UTF-16")));
            JavaFileBatch batch = new JavaFileBatch();
            boolean isClassDecorator = getTarget() == Target.TYPE;
            Class<?> driver = isClassDecorator ? classDriver() : methodDriver();

            for (Element e : roundEnv.getElementsAnnotatedWith(annotation())) {
                TypeElement te;
                if (isClassDecorator)
                    te = (TypeElement) e;
                else
                    te = (TypeElement) e.getEnclosingElement();

                if (te.getKind() == ElementKind.CLASS && claimType(te)) {
                    String qn = te.getQualifiedName().toString();
                    String newName = qn.substring(qn.lastIndexOf('.') + 1) + "$$" + name() + "$Decorated";
                    String newPkg = qn.substring(0, qn.lastIndexOf('.'));
                    String newQn = newPkg + "." + newName;
                    AnnotationSpec generatedSpec = AnnotationSpec.builder(Generated.class)
                            .addMember("value", "$S", pkg() + "." + name())
                            .build();
                    batch.newClass(newPkg, newName, b -> {
                        b.addAnnotation(generatedSpec)
                                .addModifiers(Modifier.PUBLIC);


                        Decorated decoratedAnnotation = te.getAnnotation(Decorated.class);
                        TypeMirror decoratedAnnotationType = decoratedAnnotation == null ? te.asType() : annotationHack(decoratedAnnotation::originalClass);
                        String originalClass = ((TypeElement) getTypeUtils().asElement(decoratedAnnotationType)).getQualifiedName().toString();
                        AnnotationSpec decoratedSpec = AnnotationSpec.builder(Decorated.class)
                                .addMember("originalClass",
                                        "$L.class", originalClass)
                                .build();
                        b.addAnnotation(decoratedSpec);

                        b.superclass(TypeName.get(te.asType()));

                        //Implement added interfaces
                        for (String i : interfaces()) {
                            b.addSuperinterface(ClassName.bestGuess(i));
                        }

                        //Initialize the driver
                        b.addField(FieldSpec
                                .builder(TypeName.get(driver), "__DRIVER__",
                                        Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T();", driver))
                                .addAnnotation(generatedSpec)
                                .build());
                        b.addField(FieldSpec
                                .builder(TypeName.get(Class.class), "__ORIGINAL_CLASS__",
                                        Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("$L.class;", originalClass))
                                .addAnnotation(generatedSpec)
                                .build());

                        //Force static init of superclass
                        b.addStaticBlock(CodeBlock.of("try { $T.forName($S); } catch ($T e) {}", Class.class, qn,
                                Throwable.class));

                        //Transfer all annotations except for the current decorator and a potential @Generated annotation
                        te.getAnnotationMirrors().forEach(am -> {
                            TypeElement element = (TypeElement) am.getAnnotationType().asElement();
                            if (!element.equals(generated) && !element.equals(myAnnotation) && !element.equals(decorated))
                                b.addAnnotation(AnnotationSpec.get(am));
                        });

                        List<ExecutableElement> methodAnnotationIndexTmp = new ArrayList<>();
                        if (isClassDecorator) {
                            b.addField(FieldSpec.builder(TypeName.get(AnnotationDefinition.class), "__DECORATOR_INST__",
                                    Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                    .initializer(CodeBlock.of("$L;", new AnnotationDefinition(te.getAnnotation(annotation())).builderCode()))
                                    .addAnnotation(generatedSpec)
                                    .build());
                        } else {
                            List<CodeBlock> arrayInitializers = new ArrayList<>();

                            roundEnv.getElementsAnnotatedWith(annotation())
                                    .stream()
                                    .filter(it -> it.getEnclosingElement().equals(te))
                                    .map(ExecutableElement.class::cast)
                                    .peek(ee -> {
                                        arrayInitializers.add(CodeBlock.of("$L", new AnnotationDefinition(ee.getAnnotation(annotation())).builderCode()));
                                    })
                                    .forEach(methodAnnotationIndexTmp::add);

                            b.addField(FieldSpec.builder(TypeName.get(AnnotationDefinition[].class), "__DECORATOR_INST__",
                                    Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                    .initializer(CodeBlock.join(Arrays.asList(CodeBlock.of("new $T{", TypeName.get(AnnotationDefinition[].class)),
                                            CodeBlock.join(arrayInitializers, ", "),
                                            CodeBlock.of("};")), ""))
                                    .addAnnotation(generatedSpec)
                                    .build());
                        }

                        //Collect things to be decorated
                        Map<MethodDefinition, ExecutableElement> md2ee = new HashMap<>();

                        //First we need to collect ALL elements
                        List<ExecutableElement> constructors;
                        constructors = te.getEnclosedElements()
                                .stream()
                                .filter(ee -> ee instanceof ExecutableElement)
                                .map(ExecutableElement.class::cast)
                                .filter(ee -> ee.getKind() == ElementKind.CONSTRUCTOR)
                                .collect(Collectors.toList());

                        List<ExecutableElement> methods;
                        if (isClassDecorator)
                            methods = te.getEnclosedElements()
                                    .stream()
                                    .filter(ee -> ee instanceof ExecutableElement)
                                    .map(ExecutableElement.class::cast)
                                    .filter(ee -> ee.getKind() != ElementKind.CONSTRUCTOR)
                                    .collect(Collectors.toList());
                        else {
                            methods = methodAnnotationIndexTmp;
                        }

                        List<VariableElement> fields = te.getEnclosedElements()
                                .stream()
                                .filter(ee -> ee instanceof VariableElement)
                                .map(VariableElement.class::cast)
                                .filter(ee -> ee.getKind() == ElementKind.FIELD)
                                .filter(ee -> ee.getAnnotation(Generated.class) == null)
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
                                .map(ee -> {
                                    MethodDefinition md = executableElementToMethodDef(ee);
                                    md2ee.put(md, ee);
                                    return md;
                                })
                                .forEach(staticMethodDefs::add);

                        Arrays.stream(methods())
                                .filter(MethodDefinition::isStatic)
                                .forEach(staticMethodDefs::add);


                        Set<MethodDefinition> instanceMethodDefs = new HashSet<>();
                        methods.stream()
                                .filter(ee -> !ee.getModifiers().contains(Modifier.STATIC))
                                .map(ee -> {
                                    MethodDefinition md = executableElementToMethodDef(ee);
                                    md2ee.put(md, ee);
                                    return md;
                                })
                                .forEach(instanceMethodDefs::add);

                        interfaceMethods.forEach(m -> instanceMethodDefs.add(MethodDefinition.from(m)));

                        Arrays.stream(methods())
                                .filter(md -> !md.isStatic())
                                .forEach(instanceMethodDefs::add);

                        Set<MethodDefinition> annotatedMethods = methods.stream()
                                .filter(ee -> ee.getAnnotation(annotation()) != null)
                                .map(this::executableElementToMethodDef)
                                .collect(Collectors.toSet());


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
                                .addAnnotation(generatedSpec)
                                .build());

                        List<CodeBlock> staticFieldBindings = new ArrayList<>();
                        staticFieldDefs.stream()
                                .filter(FieldDefinition::isStatic)
                                .forEach(v -> {
                                    String name = v.getName();
                                    staticFieldBindings.add(
                                            CodeBlock.of(String.format("__STATIC_FIELD_PROXY__.bind($S, new $T(" +
                                                            "$S, $L, $L, $T.class, %s, %s);",
                                                    makeGetterTemplate(v, false, qn), makeSetterTemplate(v, false, qn, name)),
                                                    name,
                                                    FieldBinding.class,
                                                    name,
                                                    true,
                                                    v.getModifiers(),
                                                    v.getType(),
                                                    name));
                                });
                        b.addStaticBlock(CodeBlock.join(staticFieldBindings, "\n"));

                        b.addField(FieldSpec
                                .builder(TypeName.get(FieldProxy.class), "__INSTANCE_FIELD_PROXY__",
                                        Modifier.FINAL, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T(__STATIC_FIELD_PROXY__);", FieldProxy.class))
                                .addAnnotation(generatedSpec)
                                .build());

                        List<CodeBlock> instanceFieldBindings = new ArrayList<>();
                        instanceFieldDefs.stream()
                                .filter(v -> !v.isStatic())
                                .forEach(v -> {
                                    String name = v.getName();
                                    instanceFieldBindings.add(
                                            CodeBlock.of(String.format("__INSTANCE_FIELD_PROXY__.bind($S, new $T(" +
                                                            "$S, $L, $L, $T.class, %s, %s);",
                                                    makeGetterTemplate(v, true, qn), makeSetterTemplate(v, true, qn, name)),
                                                    name,
                                                    FieldBinding.class,
                                                    name,
                                                    true,
                                                    v.getModifiers(),
                                                    v.getType(),
                                                    name));
                                });
                        b.addMethod(MethodSpec.methodBuilder("__FIELD_PROXY_INIT__")
                                .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                                .addCode(CodeBlock.join(instanceFieldBindings, "\n"))
                                .build());

                        //Proxy methods and bootstrap
                        b.addField(FieldSpec
                                .builder(TypeName.get(MethodProxy.class), "__STATIC_METHOD_PROXY__",
                                        Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T();", MethodProxy.class))
                                .addAnnotation(generatedSpec)
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
                                            CodeBlock.of("__STATIC_METHOD_PROXY__.bind($S, new " +
                                                            "$T(" +
                                                            "$S, $L, $T.class, new Class[]{$L}, $L));",
                                                    name,
                                                    MethodBinding.class,
                                                    name,
                                                    m.getModifiers(),
                                                    m.getReturnType(),
                                                    String.join(", ", args),
                                                    generateInvoker(qn, m)));
                                });
                        b.addStaticBlock(CodeBlock.join(staticMethodBindings, "\n"));

                        b.addField(FieldSpec
                                .builder(TypeName.get(ConstructorProxy.class), "__CONSTRUCTOR_PROXY__",
                                        Modifier.FINAL, Modifier.STATIC, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T();", ConstructorProxy.class))
                                .addAnnotation(generatedSpec)
                                .build());

                        List<CodeBlock> constructorBindings = new ArrayList<>();
                        constructors.forEach(ee -> {
                            List<String> args = ee.getParameters()
                                    .stream()
                                    .map(p -> toClass(p).getCanonicalName() + ".class")
                                    .collect(Collectors.toList());
                            List<String> argStringTemp = new ArrayList<>();
                            for (int i = 0; i < ee.getParameters().size(); i++) {
                                argStringTemp.add("("
                                        + toClass(ee.getParameters().get(i)).getCanonicalName()
                                        + ") args[" + i + "]");
                            }
                            String argStr = String.join(", ", argStringTemp);
                            constructorBindings.add(
                                    CodeBlock.of("__CONSTRUCTOR_PROXY__.bind(new " +
                                                    "$T(" +
                                                    "$S, $L, $L.class, new Class[]{$L}, (args) -> new $L($L)));",
                                            MethodBinding.class,
                                            "<init>",
                                            java.lang.reflect.Modifier.PUBLIC,
                                            newQn,
                                            String.join(", ", args),
                                            newQn,
                                            argStr));
                        });
                        b.addStaticBlock(CodeBlock.join(constructorBindings, "\n"));

                        b.addField(FieldSpec
                                .builder(TypeName.get(MethodProxy.class), "__INSTANCE_METHOD_PROXY__",
                                        Modifier.FINAL, Modifier.PRIVATE)
                                .initializer(CodeBlock.of("new $T(__STATIC_METHOD_PROXY__);", MethodProxy.class))
                                .addAnnotation(generatedSpec)
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
                                            CodeBlock.of("__INSTANCE_METHOD_PROXY__.bind($S, " +
                                                            "new $T(" +
                                                            "$S, $L, $T.class, new Class[]{$L}, $L));",
                                                    name,
                                                    MethodBinding.class,
                                                    name,
                                                    m.getModifiers(),
                                                    m.getReturnType(),
                                                    String.join(", ", args),
                                                    generateInvoker(qn, m)));
                                });
                        b.addMethod(MethodSpec.methodBuilder("__METHOD_PROXY_INIT__")
                                .addModifiers(Modifier.FINAL, Modifier.PRIVATE)
                                .addCode(CodeBlock.join(instanceMethodBindings, "\n"))
                                .build());

                        String optionalFirstParam = isClassDecorator ? "__DECORATOR_INST__, " : "";
                        b.addStaticBlock(CodeBlock.of("__DRIVER__.runtimeInit(" + optionalFirstParam + "__ORIGINAL_CLASS__, __STATIC_FIELD_PROXY__);"));

                        constructors.forEach(con -> {
                            MethodSpec.Builder builder = MethodSpec.constructorBuilder()
                                    .addModifiers(Modifier.PUBLIC)
                                    .addAnnotation(decoratedSpec);

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

                            builder.addStatement("super(" + argListString + ")");

                            builder.addStatement("this.__FIELD_PROXY_INIT__()");
                            builder.addStatement("this.__METHOD_PROXY_INIT__()");

                            if (isClassDecorator) {
                                builder.addStatement("__DRIVER__.init(__DECORATOR_INST__, __ORIGINAL_CLASS__, this.__INSTANCE_FIELD_PROXY__, " +
                                        "this.__INSTANCE_METHOD_PROXY__$L)", argListString.isEmpty()
                                        ? "" : ", " + argListString);
                            }

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
                            if (isClassDecorator || annotatedMethods.contains(md)) {
                                MethodSpec.Builder builder = MethodSpec.methodBuilder(md.getName())
                                        .addModifiers(reflectModsToProcessorMods(md.getModifiers()))
                                        .addAnnotation(decoratedSpec)
                                        .returns(md.getReturnType());

                                for (int i = 0; i < md.getArgTypes().length; i++) {
                                    Class<?> param = md.getArgTypes()[i];
                                    builder.addParameter(ParameterSpec.builder(param, "arg" + i).build());
                                }

                                boolean isStatic = java.lang.reflect.Modifier.isStatic(md.getModifiers());
                                int argLen = md.getArgTypes().length;
                                String stmt = String.format("__%1$s_FIELD_PROXY__, __%1$s_METHOD_PROXY__, " +
                                                "__%1$s_METHOD_PROXY__",
                                        isStatic ? "STATIC" : "INSTANCE");
                                String temp = md.getReturnType().equals(void.class) ? "" : "return (" + md.getReturnType().getCanonicalName() + ") ";

                                String optionalDecoratorIndex = isClassDecorator ? "" : "[" + methodAnnotationIndexTmp.indexOf(md2ee.get(md)) + "]";
                                builder.addStatement(temp + "__DRIVER__.methodWrap(__DECORATOR_INST__" + optionalDecoratorIndex + ", __ORIGINAL_CLASS__, $L.getBinding($S, new " +
                                                "Class[]{$L}$L))",
                                        stmt, md.getName(),
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
                            }
                        });

                        index.index(qn, newPkg + "." + newName);

                        return b.build();
                    });
                }
            }

            try {
                batch.publish(getFiler());
                index.export(hackStream(StandardLocation.CLASS_OUTPUT, "blackhole/decorated.idx"));
            } catch (IOException e) {
                error("Exception caught running generated decorator processor!", e);
            }
        } catch (Throwable t) {
            error(t);
            return false;
        }

        return true;
    }

    public enum Target {
        METHOD, TYPE
    }
}
