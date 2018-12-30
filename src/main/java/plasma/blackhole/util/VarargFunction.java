package plasma.blackhole.util;

import java.util.function.Function;

@FunctionalInterface
public interface VarargFunction<I, O> extends Function<I[], O> {

    @Override
    O apply(I... i);
}
