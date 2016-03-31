package cl.jdbc;

import static cl.core.decorator.exception.ExceptionDecorators.uncheck;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * A thin wrapper around a file which contains an SQLite database.
 * This class does not guarantee thread safety.  Also, many objects
 * of this class may point to the same physical file.
 */
public class SQLiteDB {

    private static final String JDBC_DRIVER = "org.sqlite.JDBC";
    private static final String JDBC_URL_PREFIX = "jdbc:sqlite:";
    
    private final Path path;

    /**
     * <p>Create an instance, given a path to a file.</p>
     * <p>If no file exists, it will be created whenever the first SQL statement is executed
     * against it.</p>
     * 
     * @param path  Path to a DB file
     * @param initSqlStatements List of SQL statements to execute against the DB.
     */
    public SQLiteDB(Path path, List<String> initSqlStatements) {
        if (path == null) {
            throw new IllegalArgumentException("SQLite DB path is null");
        }

        uncheck(() ->  { 
            if (!Files.exists(path)) Files.createDirectories(path);
         });
        
        this.path = path;
        
        if (initSqlStatements != null) {
            DBClient client = getClient();
            initSqlStatements.forEach(client::update);
        }
    }
    
    /**
     * <p>Create an instance, given a path to a file.</p>
     * <p>If no file exists, it will be created whenever the first SQL statement is executed
     * against it.</p>
     * 
     * @param path  Path to a DB file
     */
    public SQLiteDB(Path path) {
        this(path, null);
    }

    /**
     * Delete underlying physical file.
     */
    public void delete() {
        uncheck(() -> Files.deleteIfExists(path));
    }
    
    /**
     * Get DB client which points to the underlying SQLite database.
     */
    public DBClient getClient() {
        return DBClient.getClient(JDBC_DRIVER, JDBC_URL_PREFIX + path, null, null);
    }

    /**
     * Get path to the underlying physical file.
     */
    public Path getPath() {
        return path;
    }

}
