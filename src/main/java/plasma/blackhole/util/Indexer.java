package plasma.blackhole.util;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Indexer<T> {

    private static final char SEP = '\u200e';

    private final Map<String, T> index = new HashMap<>();
    private final Function<T, String> serializer;

    public Indexer(Function<T, String> serializer) {
        this.serializer = serializer;
    }

    public void index(String s, T obj) {
        index.put(s, obj);
    }

    public T find(String k) {
        return index.get(k);
    }

    public Set<String> keys() {
        return index.keySet();
    }

    public Collection<T> values() {
        return index.values();
    }

    public Set<Map.Entry<String, T>> entrySet() {
        return index.entrySet();
    }

    public Stream<Map.Entry<String, T>> entries() {
        return index.entrySet().stream();
    }

    public void export(OutputStream stream1) {
        try (PrintStream stream = new PrintStream(stream1)) {
            for (Map.Entry<String, T> entry : index.entrySet()) {
                String key = entry.getKey();
                T value = entry.getValue();
                stream.print(key);
                stream.print(SEP);
                stream.print(serializer.apply(value));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Indexer<T> readIndex(Function<String, T> deserializer,
                                           Function<T, String> serializer,
                                           InputStream stream) {
        Indexer<T> i = new Indexer<>(serializer);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String read = reader.lines().collect(Collectors.joining("\n"));
            StringBuilder key = null;
            StringBuilder value = null;
            for (char c : read.toCharArray()) {
                if (c == SEP) {
                    if (key == null) {
                        throw new IllegalArgumentException("Invalid index");
                    } else {
                        if (value == null) {
                            value = new StringBuilder();
                        } else {
                            i.index(key.toString(), deserializer.apply(value.toString()));
                            key = null;
                            value = null;
                        }
                    }
                } else if (key == null) {
                    key = new StringBuilder(String.valueOf(c));
                } else if (value == null) {
                    key.append(c);
                } else {
                    value.append(c);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return i;
    }
}
