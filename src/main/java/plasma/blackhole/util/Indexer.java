package plasma.blackhole.util;

import java.io.*;
import java.nio.charset.Charset;
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

    public boolean hasKey(String k) {
        return index.containsKey(k);
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

    public T forwardLookup(String k) {
        T first = find(k);
        if (first instanceof String && hasKey((String) first)) {
            return forwardLookup((String) first);
        }
        return first;
    }

    public void export(OutputStream stream1) {
        try (OutputStreamWriter stream = new OutputStreamWriter(stream1, Charset.forName("UTF-16"))) {
            for (Map.Entry<String, T> entry : index.entrySet()) {
                String key = entry.getKey();
                T value = entry.getValue();
                stream.append(key);
                stream.append(SEP);
                stream.append(serializer.apply(value));
                stream.append(SEP);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Indexer<T> readIndex(Function<String, T> deserializer,
                                           Function<T, String> serializer,
                                           InputStream stream) {
        Indexer<T> i = new Indexer<>(serializer);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-16")))) {
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

            if (key != null) {
                if (value == null) throw new IllegalArgumentException("Corrupted index?");

                //Cleanup remaining data
                i.index(key.toString(), deserializer.apply(value.toString()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return i;
    }

    public static <T> Indexer<T> readIndex(Function<String, T> deserializer,
                                           Function<T, String> serializer,
                                           File file) {
        try {
            return readIndex(deserializer, serializer, new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Indexer<String> readStringIndex(InputStream stream) {
        return readIndex(s -> s, s -> s, stream);
    }

    public static Indexer<String> readStringIndex(File file) {
        return readIndex(s -> s, s -> s, file);
    }

    public static Indexer<String> readStringIndex(String s) {
        return readStringIndex(new ByteArrayInputStream(s.getBytes()));
    }
}
