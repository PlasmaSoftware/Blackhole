package plasma.blackhole.util.internal;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Objects;

public final class ResourceUtils {

    public static String readFile(Filer filer, String path) {
        return readFile(filer, path, StandardLocation.CLASS_OUTPUT);
    }

    public static String readFile(Filer filer, String path, Charset charset) {
        return readFile(filer, path, StandardLocation.CLASS_OUTPUT, charset);
    }

    public static String readFile(Filer filer, String path, StandardLocation location) {
        return readFile(filer, path, location, Charset.defaultCharset());
    }

    public static String readFile(Filer filer, String path, StandardLocation location, Charset charset) {
        path = path.replace('\\', '/');
        try {
            FileObject fo = filer.getResource(location, "", path);
            try {
                return readFile(fo.openInputStream(), charset);
            } catch (Exception e) {
                return readFile(new FileInputStream(new File(fo.getName().replace("/test/classes/", "/production/resources/"))), charset);
            }
        } catch (IOException e) {
//            if (e instanceof FileNotFoundException path.contains("java/test"))
//                return readFile(filer, path.replace("java/test", "java/main"));
            throw new RuntimeException(e);
        }
    }

    public static String readFile(String path) {
        return readFile(path, Charset.defaultCharset());
    }

    public static String readFile(String path, Charset charset) {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        return readFile(classLoader.getResourceAsStream(path), charset);
    }

    private static String readFile(InputStream is, Charset charset) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is), charset))) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(s -> sb.append(s).append("\n"));
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: Make more efficient
    public static String readFileOrEmpty(Filer filer, String path) {
        return readFileOrEmpty(filer, path, Charset.defaultCharset());
    }

    public static String readFileOrEmpty(Filer filer, String path, Charset charset) {
        try {
            return readFile(filer, path, charset);
        } catch (Exception e) {
            return "";
        }
    }

    public static String readFileOrEmpty(String path) {
        return readFileOrEmpty(path, Charset.defaultCharset());
    }

    public static String readFileOrEmpty(String path, Charset charset) {
        try {
            return readFile(path, charset);
        } catch (Exception e) {
            return "";
        }
    }
}
