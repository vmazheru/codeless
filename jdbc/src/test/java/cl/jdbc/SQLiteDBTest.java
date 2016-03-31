package cl.jdbc;

import static cl.core.decorator.exception.ExceptionDecorators.*;
import static org.junit.Assert.*;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/**
 * Simple test illustrating the use of SQLiteDB
 */
public class SQLiteDBTest {

    /**
     * Test creating with temporary file, execute queries with DBClient, and delete
     */
    @Test
    public void testSQLiteDB() {
        SQLiteDB db = null;
        try {
            // create a new DB (when no file exists yet)
            List<String> statements = Arrays.asList(
                    "CREATE TABLE test (id, value)",
                    "INSERT INTO test VALUES (1, 'test value')");
            
            db = new SQLiteDB(uncheck(() -> Files.createTempFile("db", "")), statements);
            
            // execute queries
            DBClient client = db.getClient();
            String value = client.getScalar("SELECT value FROM test WHERE id = 1");
            assertEquals("test value", value);
            
            // create a new DB from existing file
            SQLiteDB db2 = new SQLiteDB(db.getPath());
            assertEquals("test value", db2.getClient().getScalar("SELECT value FROM test WHERE id = 1"));
        } finally {
            if (db != null) {
                db.delete();
                assertTrue(Files.notExists(db.getPath()));
            }
        }
    }
    
}
