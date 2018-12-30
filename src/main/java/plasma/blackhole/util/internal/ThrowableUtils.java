package plasma.blackhole.util.internal;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ThrowableUtils {

    public static String toString(Throwable t) {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        t.printStackTrace(printWriter);
        return writer.toString();
    }
}
