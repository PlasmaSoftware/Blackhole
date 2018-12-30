package plasma.blackhole.util.internal;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A binary search tree which locates a leaf node through an ordered set of arbitrary attributes (no need for them to
 * have a natural ordering).
 *
 * @implNote Attributes and values must be non-null!
 */
public class AttributeSearchTree<A, T> {

    public final static Object EMPTY = new Object();

    private final Junction<A, T> root = new Junction<>();
    private final Function<T, A[]> attributeExtractor;

    public AttributeSearchTree(Function<T, A[]> attributeExtractor) {
        this.attributeExtractor = attributeExtractor;
    }

    public AttributeSearchTree<A, T> addObject(T obj) {
        A[] attrs = attributeExtractor.apply(Objects.requireNonNull(obj));
        for (A attr : attrs) Objects.requireNonNull(attr);

        Junction<A, T> lastNode = null;
        Node<T> node = root;
        for (A attr : attrs) {
            if (node instanceof Junction) {
                if (((Junction<A, T>) node).canSplitAt(attr)) {
                    lastNode = (Junction<A, T>) node;
                    node = ((Junction<A, T>) node).splitAt(attr);
                } else {
                    lastNode = (Junction<A, T>) node;
                    node = ((Junction<A, T>) node).extendDomain(attr, new Junction<>());
                }
            } else {
                Junction<A, T> newNode = new Junction<>();
                newNode.setIntermediateValue(node.getValue());
                lastNode.extendDomain(lastNode.getKey(node.getValue()), newNode);
                newNode.extendDomain(attr, new Junction<>());
                lastNode = newNode;
                node = newNode.splitAt(attr);
            }
        }

        if (node instanceof Leaf) {
            throw new AssertionError("This should never happen");
        } else {
            ((Junction<A, T>) node).setIntermediateValue(obj);
        }

        return this;
    }

    public T findObject(A[] attrs) {
        Node<T> node = root;
        for (int i = 0; i < attrs.length; i++) {
            A attr = attrs[i];
            node = ((Junction) node).splitAt(attr);
            if (i == attrs.length - 1) {
                if (!node.hasValue())
                    throw new NoSuchElementException();
                else
                    return node.getValue();
            } else {
                if (!(node instanceof Junction))
                    throw new NoSuchElementException();
            }
        }

        if (!node.hasValue())
            throw new NoSuchElementException();

        return node.getValue();
    }

    //Allows for an expressive depth first search
    public T interactiveTraversal(Interactor<A> interactor) {
        return interactiveTraversalContinued(interactor, root, 0, 0, root.keySet().size());
    }

    private T interactiveTraversalContinued(Interactor<A> interactor, Node<T> node, int level, int currLevelCount, int limit) {
        if (!(node instanceof Junction)) //Unexpected leaf, fail
            return null;

        //noinspection unchecked
        Object o = interactor.selectPoint(((Junction) node).getDomain(), level, currLevelCount);
        if (o == null) //No point selected, fail this search
            return null;
        else {
            //noinspection unchecked
            Node<T> newNode = ((Junction) node).splitAt(o);
            if (EMPTY.equals(o)) { //We got a leaf so return the value
                if (newNode.hasValue()) {
                    return newNode.getValue();
                } else {
                    return null;
                }
            } else {
                //Increase the depth
                T o2 = interactiveTraversalContinued(interactor, newNode, level + 1, 0,
                        newNode instanceof Junction ? ((Junction) newNode).keySet().size() : 1);
                if (o2 != null) return o2; //Success, so propagate the result

                if (currLevelCount == limit) return null; //Reached the possible paths of this level so fail

                // Move onto the next possible branch of this junction
                return interactiveTraversalContinued(interactor, node, level, currLevelCount + 1, limit);
            }
        }
    }

    private interface Node<T> {

        boolean hasValue();

        T getValue();
    }

    private static final class Junction<A, T> implements Node<T> {

        // We enforce typing internally, so we don't have to care about this
        private final Map<Object, Node<T>> domain = new HashMap<>();

        public Node<T> extendDomain(A key, Node<T> value) {
            domain.put(key, value);
            return value;
        }

        public Node<T> setIntermediateValue(T value) {
            domain.put(EMPTY, new Leaf<>(value));
            return this;
        }

        public Node<T> splitAt(A key) {
            return domain.get(key);
        }

        public boolean canSplitAt(A key) {
            return domain.containsKey(key);
        }

        @SuppressWarnings("unchecked")
        public A getKey(T val) {
            return (A) domain.entrySet()
                    .stream()
                    .filter(e -> e.getValue().equals(val))
                    .map(Map.Entry::getKey)
                    .findFirst().get();
        }

        public Set<Object> keySet() {
            return domain.keySet();
        }

        public Set<A> getDomain() {
            return keySet().stream().filter(o -> o != EMPTY).map(o -> (A) o).collect(Collectors.toSet());
        }

        @Override
        public boolean hasValue() {
            return domain.containsKey(EMPTY);
        }

        @Override
        public T getValue() {
            return domain.get(EMPTY).getValue();
        }
    }

    private static final class Leaf<T> implements Node<T> {

        private final T value;

        private Leaf(T value) {
            this.value = value;
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public T getValue() {
            return value;
        }
    }

    @FunctionalInterface
    public interface Interactor<T> {

        Object selectPoint(Set<T> choices, int level, int levelVisitedCount);
    }
}
