package plasma.blackhole.util.internal;

import javax.lang.model.type.TypeMirror;

public class ClassTypeProxy {

    private final TypeMirror mirror;

    public ClassTypeProxy(TypeMirror mirror) {
        this.mirror = mirror;
    }

    public String toLiteral() {
        return mirror.toString() + ".class";
    }
}
