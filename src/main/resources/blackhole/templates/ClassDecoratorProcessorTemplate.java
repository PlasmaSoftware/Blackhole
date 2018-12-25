package ${package};

import javax.annotation.Generated;
import javax.annotation.processing.Processor;
import java.lang.annotation.Annotation;

import com.austinv11.servicer.WireService;
import plasma.blackhole.processor.AbstractDecoratorImplProcessor;
import plasma.blackhole.api.ClassDecoratorDriver;
import plasma.blackhole.api.MethodDecoratorDriver;
import plasma.blackhole.util.FieldDefinition;
import plasma.blackhole.util.MethodDefinition;
import ${driver_package}.${driver};


@Generated("plasma.blackhole.DecoratorAnnotationProcessor")
@WireService(Processor.class)
public class ${name} extends AbstractDecoratorImplProcessor {

    @Override
    public AbstractDecoratorImplProcessor.Target getTarget() {
        return AbstractDecoratorImplProcessor.Target.TYPE;
    }

    @Override
    public Class<? extends Annotation>[] annotations() {
        return new Class[]{${annotation}};
    }

    @Override
    public ClassDecoratorDriver classDriver() {
        return ${driver}.class;
    }

    @Override
    public MethodDecoratorDriver methodDriver() {
        return null;
    }

    @Override
    public String pkg() {
        return "${package}";
    }

    @Override
    public String name() {
        return "${annotation}";
    }

    @Override
    public String[] interfaces() {
        return new String[]{${interfaces}};
    }

    @Override
    public FieldDefinition[] fields() {
        return new FieldDefinition[]{${fields}};
    }

    @Override
    public MethodDefinition[] methods() {
        return new MethodDefinition[]{${methods}};
    }
}