package plasma.blackhole.test;

import org.junit.Test;
import plasma.blackhole.util.Indexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.Assert.*;

public class TestIndexer {

    @Test
    public void testIndex() throws IOException {
        Indexer<String> index = Indexer.readStringIndex("");
        assertNull(index.find("hello"));
        index.index("hello", "world");
        assertEquals("world", index.find("hello"));
        assertEquals("world", index.forwardLookup("hello"));

        File temp = File.createTempFile("TestIndexer.testIndex", "blackhole");
        temp.deleteOnExit();
        index.export(new FileOutputStream(temp));

        index = Indexer.readStringIndex(temp);
        assertEquals("world", index.find("hello"));
        assertEquals("world", index.forwardLookup("hello"));
    }

    @Test
    public void testForwardLookup() throws IOException {
        Indexer<String> index = Indexer.readStringIndex("");
        assertNull(index.find("hello"));
        index.index("hello", "world");
        index.index("world", "hello2");
        index.index("hello2", "world2");
        assertEquals("world", index.find("hello"));
        assertEquals("hello2", index.find("world"));
        assertEquals("world2", index.find("hello2"));
        assertEquals("world2", index.forwardLookup("hello"));

        File temp = File.createTempFile("TestIndexer.testForwardLookup", "blackhole");
        temp.deleteOnExit();
        index.export(new FileOutputStream(temp));

        index = Indexer.readStringIndex(temp);
        assertEquals("world", index.find("hello"));
        assertEquals("hello2", index.find("world"));
        assertEquals("world2", index.find("hello2"));
        assertEquals("world2", index.forwardLookup("hello"));
    }
}
