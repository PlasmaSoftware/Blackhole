package plasma.blackhole.util.internal;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public final class NameUtils {

    private static final Random rng = new Random(ThreadLocalRandom.current().nextLong());

    private static String randIntAsString() {
        StringBuilder i = new StringBuilder();
        for (int j = 0; j < 5; j++) {
            i.append(rng.nextInt());
        }
        return i.toString();
    }

    public static String addNameEntropy(String name) {
        return name + "$$" + randIntAsString();
    }
}
