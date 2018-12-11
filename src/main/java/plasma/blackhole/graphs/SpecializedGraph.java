package plasma.blackhole.graphs;

import com.austinv11.graphs.alg.TopologicalSortStrategy;
import com.austinv11.graphs.impl.DirectedAcyclicGraph;
import com.austinv11.graphs.impl.SimpleEdge;
import com.austinv11.graphs.impl.SimpleGraph;
import com.austinv11.graphs.impl.SimpleVertex;

import java.util.Iterator;

/**
 * Wrapper for SimpleGraph's DAG to make its use simpler as we only care about the topological sort of elements and we
 * can make certain assumptions about our use case. For example, this is not thread-safe and we do not allow removal
 * of nodes/edges.
 */
class SpecializedGraph<T> implements Iterable<T> {

    private final DirectedAcyclicGraph<T, SimpleVertex<T>, SimpleEdge<T, SimpleVertex<T>>> dag
            = new DirectedAcyclicGraph<>(new SimpleGraph<>(false));

    public SpecializedGraph<T> addVertex(T vertex) {
        dag.addVertex(new SimpleVertex<>(vertex));
        return this;
    }

    public SpecializedGraph<T> addEdge(T from, T to) {
        SimpleVertex<T> f = dag.findVertex(from), t = dag.findVertex(to);
        if (f == null)
            f = new SimpleVertex<>(from);
        if (t == null)
            t = new SimpleVertex<>(to);
        dag.addEdge(SimpleEdge.builder(f, t).setDirected(true).build());
        return this;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Iterator<SimpleVertex<T>> v = dag.sortVertices(new TopologicalSortStrategy<>()).iterator();

            @Override
            public boolean hasNext() {
                return v.hasNext();
            }

            @Override
            public T next() {
                return v.next().get();
            }
        };
    }
}
