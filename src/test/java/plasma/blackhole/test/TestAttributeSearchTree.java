package plasma.blackhole.test;

import org.junit.Test;
import plasma.blackhole.util.MethodBinding;
import plasma.blackhole.util.MethodProxy;
import plasma.blackhole.util.internal.AttributeSearchTree;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;

public class TestAttributeSearchTree {

    @org.junit.Test
    public void testAST() {
        AttributeSearchTree<Integer, Test> ast = new AttributeSearchTree<>(t -> new Integer[]{t.attr1, t.attr2, t.attr3});
        Test test1 = new Test(1, 2, 3, "Hello");
        Test test2 = new Test(3, 2, 1, "World");
        ast.addObject(test1).addObject(test2);

        assertEquals(test1, ast.findObject(new Integer[]{1, 2, 3}));
        assertEquals(test2, ast.findObject(new Integer[]{3, 2, 1}));
    }

    @org.junit.Test(expected = NoSuchElementException.class)
    public void testASTError() {
        AttributeSearchTree<Integer, Test> ast = new AttributeSearchTree<>(t -> new Integer[]{t.attr1, t.attr2, t.attr3});
        Test test1 = new Test(1, 2, 3, "Hello");
        Test test2 = new Test(3, 2, 1, "World");
        ast.addObject(test1).addObject(test2);

        ast.findObject(new Integer[]{1, 2});
    }

    @org.junit.Test
    public void testMethodProxyAST() {
        MethodProxy proxy = new MethodProxy();
        proxy.bind("test", new MethodBinding("test", 0, void.class, new Class[]{int.class},
                a -> {throw new RuntimeException("dummy");}));
        MethodBinding binding2 = new MethodBinding("test", 0, void.class, new Class[]{int.class, int.class},
                a -> {throw new RuntimeException("dummy");});
        proxy.bind("test", binding2);
        MethodBinding binding3 = new MethodBinding("test", 0, void.class, new Class[]{int.class, String.class},
                a -> {throw new RuntimeException("dummy");});
        proxy.bind("test", binding3);

        assertEquals(binding2, proxy.bestGuessBinding("test", 1, 2));
        assertEquals(binding3, proxy.bestGuessBinding("test", 1, "Hello"));
        assertEquals(binding3, proxy.bestGuessBinding("test", 1, null));
    }

    private static class Test {
        private final int attr1, attr2, attr3;
        private final String msg;

        private Test(int attr1, int attr2, int attr3, String msg) {
            this.attr1 = attr1;
            this.attr2 = attr2;
            this.attr3 = attr3;
            this.msg = msg;
        }
    }
}
