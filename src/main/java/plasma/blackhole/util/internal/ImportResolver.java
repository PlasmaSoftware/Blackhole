package plasma.blackhole.util.internal;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner8;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

//Based https://stackoverflow.com/a/18777229
//Note: this is not perfect! The answer lists some limitations, however this impl modifies the original such that:
//1) Generics are stripped
public class ImportResolver extends ElementScanner8<Void, Void> {

    private Set<String> types = new HashSet<>();

    public Set<String> getImportedTypes() {
        return types.stream().map(s -> {
            if (s.contains("<")) {
                return s.substring(0, s.indexOf("<"));
            } else {
                return s;
            }
        }).collect(Collectors.toSet());
    }

    @Override
    public Void visitType(TypeElement e, Void p) {
        for(TypeMirror interfaceType : e.getInterfaces()) {
            types.add(interfaceType.toString());
        }
        types.add(e.getSuperclass().toString());
        return super.visitType(e, p);
    }

    @Override
    public Void visitExecutable(ExecutableElement e, Void p) {
        if(e.getReturnType().getKind() == TypeKind.DECLARED) {
            types.add(e.getReturnType().toString());
        }
        return super.visitExecutable(e, p);
    }

    @Override
    public Void visitTypeParameter(TypeParameterElement e, Void p) {
        if(e.asType().getKind() == TypeKind.DECLARED) {
            types.add(e.asType().toString());
        }
        return super.visitTypeParameter(e, p);
    }

    @Override
    public Void visitVariable(VariableElement e, Void p) {
        if(e.asType().getKind() == TypeKind.DECLARED) {
            types.add(e.asType().toString());
        }
        return super.visitVariable(e, p);
    }

}
