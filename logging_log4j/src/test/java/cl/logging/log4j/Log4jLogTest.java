package cl.logging.log4j;

import static cl.core.decorator.exception.ExceptionDecorators.*;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import cl.logging.Log;

public class Log4jLogTest {
    
    private static final File logFile = new File("./target/test.log");

    @Test
    public void testLog4jLog() {
        logFile.delete();
        
        Arrays.asList(
                "cl.logging.trace",
                "cl.logging.debug",
                "cl.logging.info", 
                "cl.logging.warn",
                "cl.logging.error")
        .forEach(unchecked((String logName) -> testLevel(logName)));
    }
    
    private static void testLevel(String logName) throws Exception {
        Log log = Log.getLog(logName);
        log.trace(() -> "cl.logging.trace");
        log.debug(() -> "cl.logging.debug");
        log.info(()  -> "cl.logging.info");
        log.warn(()  -> "cl.logging.warn");
        log.error(() -> "cl.logging.error");
        
        assertTrue(logFile.exists());
        Set<String> lines = new HashSet<>(Files.readAllLines(logFile.toPath()));
        
        if ("cl.logging.trace".equals(logName)) {
            assertEquals(5, lines.size());
        } else if ("cl.logging.debug".equals(logName)) {
            assertEquals(4 + 5, lines.size());
        } else if ("cl.logging.info".equals(logName)) {
            assertEquals(3 + 4 + 5, lines.size());
        } else if ("cl.logging.warn".equals(logName)) {
            assertEquals(2 + 3 + 4 + 5, lines.size());
        } else if ("cl.logging.error".equals(logName)) {
            assertEquals(1 + 2 + 3 + 4 + 5, lines.size());
        }
        
    }
}
