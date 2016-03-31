package cl.jdbc;

import static java.util.stream.Collectors.*;

import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.sql.DataSource;

import cl.core.function.FunctionWithException;
import cl.core.util.Reflections;

/**
 * This class allows for easier execution of SQL statement against a JDBC-enabled data source.<br/>
 * It should be used primarily for testing and situations where enterprise features like transaction
 * management are not required.
 */
public interface DBClient {
    
    /**
     * Execute a multiple-column SELECT query and return the result as a list of object arrays
     * (where every array represents one row from the database).
     */
    List<Object[]> getTable(String sql);
    
    /**
     * Execute a SELECT query and map the result to objects of some type by applying a given
     * mapping function.
     */
    <T> List<T> getObjects(String sql, FunctionWithException<ResultSet, T> mapper);
    
    /**
     * Default implementation of {@code getObject()} method which reflectively matches result set
     * column names to the object's properties. 
     * 
     * @param sql   SELECT statement to run. Column names in this select should match class property names.
     * @param klass An object of this class will be instantiated for each row.
     */
    default <T> List<T> getObjects(String sql, Class<T> klass) {
        return getObjects(sql, new ReflectiveMappingFunction<>(klass));
    }
    
    /**
     * Execute a single-column SELECT query and return the result as a list of objects whose type
     * matches the database column type.
     */
    <T> List<T> getVector(String sql);
    
    /**
     * Execute a single-value SELECT query and return it as an object of some specific type.
     */
    default <T> T getScalar(String sql) { return this.<T>getVector(sql).get(0); }

    /**
     * Execute a multiple-column SELECT query and call a given function on each row
     * in the result set.
     */
    void getTable(String sql, Consumer<Object[]> onRow);
    
    /**
     * Execute a SELECT query, map a row in result set to an object of some type, and execute 
     * a given callback on each object.
     */
    <T> void getObjects(String sql, FunctionWithException<ResultSet, T> mapper, Consumer<T> onObject);
    
    /**
     * Execute a SELECT query, map every row reflectively to an object, and execute a call back
     * for each object.
     */
    default <T> void getObjects(String sql, Class<T> klass, Consumer<T> onObject) {
        getObjects(sql, new ReflectiveMappingFunction<>(klass), onObject);
    }
    
    /**
     * Execute a single-column SELECT query and call a given function on each value
     * in the result set.
     */
    <T> void getVector(String sql, Consumer<T> onValue);

    /**
     * Execute a SELECT statement and dump its result into a print writer object, where every printed row
     * represents a row from the database, and the values are separated by the given delimiter.
     */
    default void dump(String sql, CharSequence delim, PrintWriter out) {
        getTable(sql, row -> out.println(Stream.of(row).map(Objects::toString).collect(joining(delim))));
    }

    /**
     * Execute an INSERT, UPDATE, or DELETE statement and return the number of affected rows. 
     */
    int update(String sql);

    /**
     * Open a connection explicitly. Use this method if you need to execute multiple SQL statements
     * and want to save time by re-using the same database connection.<br/>
     * This connection will be held open until the client closes it with {@code closeConnection()},
     * which should be used in the finally block.<br/>
     * Repeated subsequent calls to this method will have no effect until the current connect
     * gets closed with {@code closeConnection()}
     * 
     * @see #closeConnection()
     */
    void openConnection();
    
    /**
     * Close previously explicitly open connection. If no connection is open, calling this method
     * will have no effect.
     * 
     * @see #openConnection()
     */
    void closeConnection();
    
    /**
     * Get underlying data source
     */
    DataSource getDataSource();
    
    /**
     * Create a new object, given JDBC connection parameters (a simple data source object
     * will be created out of this parameters).
     */
    static DBClient getClient(String driver, String url, String user, String password) {
        return new DBClientImpl(driver, url, user, password);
    }

    /**
     * Create a new object, given an existing data source object.
     */
    static DBClient getClient(DataSource dataSource) {
        return new DBClientImpl(dataSource);
    }
    
    /**
     * A run-time exception class instances of which may be thrown by all methods in this class.
     */
    @SuppressWarnings("serial")
    static class DBClientException extends RuntimeException {
        public DBClientException(Throwable cause) {
            super(cause);
        }
    }
}

/**
 * A function which assumes that result set column names match object properties and uses
 * reflection to initialize objects from the result set.
 */
class ReflectiveMappingFunction<T> implements FunctionWithException<ResultSet, T> {
    
    private final Class<T> klass;
    
    public ReflectiveMappingFunction(Class<T> klass) {
        this.klass = klass;
    }
    
    @Override
    public T apply(ResultSet rs) throws Exception {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        T object = klass.newInstance();
        for (int i = 1; i <= columnCount; i++) {
            String columnName = rsmd.getColumnName(i);
            Object value = rs.getObject(i);
            Reflections.set(columnName, value, object);
        }
        return object;
    }
}