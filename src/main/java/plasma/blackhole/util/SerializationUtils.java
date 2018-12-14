package plasma.blackhole.util;

import com.linkedin.paldb.api.Configuration;
import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreWriter;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public final class SerializationUtils {

    public static void write(Filer filer, String indexName, Map<String, String> map) throws IOException {
        FileObject fo = filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/" + indexName);
        try (OutputStream os = fo.openOutputStream()) {
            StoreWriter writer = PalDB.createWriter(os, new Configuration());
            for (Map.Entry<String, String> e : map.entrySet()) {
                writer.put(e.getKey(), e.getValue());
            }
            writer.close();
        }
    }
}
