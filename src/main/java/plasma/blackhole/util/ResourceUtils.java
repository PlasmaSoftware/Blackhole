package plasma.blackhole.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;

public final class ResourceUtils {

    public static String readFile(String path) {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(path))))) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(s -> sb.append(s).append("\n"));
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
