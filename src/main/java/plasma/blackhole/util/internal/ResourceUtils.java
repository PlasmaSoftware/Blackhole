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

    public static String readFile(Filer filer, String path, StandardLocation location) {
        path = path.replace('\\', '/');
        try {
            FileObject fo = filer.getResource(location, "", path);
            try {
                return readFile(fo.openInputStream());
            } catch (Exception e) {
                return readFile(new FileInputStream(new File(fo.getName().replace("/test/classes/", "/production/resources/"))));
            }
        } catch (IOException e) {
//            if (e instanceof FileNotFoundException path.contains("java/test"))
//                return readFile(filer, path.replace("java/test", "java/main"));
            throw new RuntimeException(e);
        }
    }

    public static String readFile(String path) {
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        return readFile(classLoader.getResourceAsStream(path));
    }

    private static String readFile(InputStream is) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is), Charset.forName("UTF-16")))) {
            StringBuilder sb = new StringBuilder();
            reader.lines().forEach(s -> sb.append(s).append("\n"));
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO: Make more efficient
    public static String readFileOrEmpty(Filer filer, String path) {
        try {
            return readFile(filer, path);
        } catch (Exception e) {
            return "";
        }
    }

    public static String readFileOrEmpty(String path) {
        try {
            return readFile(path);
        } catch (Exception e) {
            return "";
        }
    }
}
