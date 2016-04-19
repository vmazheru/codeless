package cl.jdbc

import javax.sql.DataSource
import java.sql.Connection
import cl.core.lang.Control.using
import scala.collection.mutable.ListBuffer
import java.sql.DriverManager
import java.io.PrintWriter
import cl.core.function.ScalaToJava._
import java.util.function.Consumer
import java.sql.ResultSet
import java.sql.Statement
import cl.core.function.ScalaToJava._
import java.sql.SQLException
import cl.core.function.FunctionWithException

/**
 * Implementation of DBClient interface.
 */
class DBClientImpl (dataSource: DataSource) extends DBClient {
  
  private[this] var conn: Connection = null
  
  def this(driver: String, url: String, user: String, password: String) {
    this(new DBClientDataSource(driver, url, user, password))
  }

  override def getTable(sql: String): java.util.List[Array[AnyRef]] = 
    queryForRows(sql, regularStatement, rsColumnCount, rsToArray)
    
  override def getObjects[T](sql: String, mapper: FunctionWithException[ResultSet, T]): java.util.List[T] =
    queryForRows(sql, regularStatement, oneColumnCount, (rs, columnCount) => mapper.apply(rs))
  
  override def getVector[T](sql: String): java.util.List[T] =
    queryForRows(sql, regularStatement, oneColumnCount, (rs, columnCount) => rs.getObject(1).asInstanceOf[T])
  
  override def getTable(sql: String, f: Consumer[Array[AnyRef]]) {
    query(sql, flatStatement, rsColumnCount, rsToArray, f)
  }
  
  override def getObjects[T](sql: String, mapper: FunctionWithException[ResultSet, T], f: Consumer[T]) =
    query(sql, flatStatement, oneColumnCount, (rs, columnCount) => mapper.apply(rs), f)
  
  override def getVector[T](sql: String, f: Consumer[T]) {
    query(sql, flatStatement, oneColumnCount, (rs, columnCount) => rs.getObject(1).asInstanceOf[T], f)
  }

  override def update(sql: String): Int = 
    withConnection { conn =>
      using(conn.createStatement()) { st =>
        st.executeUpdate(sql)
      }
    }
  
  override def openConnection() {
    if (conn == null) conn = getConnection
  }
  
  override def closeConnection() {
    if (conn != null) {
      conn.close()
      conn = null
    }
  }
  
  override def getDataSource() = dataSource
  
  private def getConnection() = dataSource.getConnection
  
  private def withConnection[R](f: Connection => R) = {
    val conn = if (this.conn != null) this.conn else getConnection
    try {
      f.apply(conn)
    } catch {
      case ex: Exception => throw new DBClient.DBClientException(ex) 
    } finally {
      if (this.conn == null) conn.close()
    }
  }
  
  private def query[T](sql: String,
      connToSt: Connection => Statement,
      rsToColumnCount: ResultSet => Int,
      rsToRow: (ResultSet, Int) => T,
      onRow: T => Unit): Unit = {
    withConnection { conn =>
      using(connToSt(conn)) { st =>
        using(st.executeQuery(sql)) { rs =>
          val columnCount = rsToColumnCount(rs)
          while (rs.next()) {
            val row = rsToRow(rs, columnCount)
            onRow(row)
          }
        }
      }
    }
  }
  
  private def queryForRows[T](sql: String,
      connToSt: Connection => Statement,
      rsToColumnCount: ResultSet => Int,
      rsToRow: (ResultSet, Int) => T): java.util.ArrayList[T] = {
      val rows = new java.util.ArrayList[T]
      query(sql, connToSt, rsToColumnCount, rsToRow, (value: T) => rows.add(value))
      rows
  }
  
  private def rsColumnCount(rs: ResultSet) = rs.getMetaData.getColumnCount
  private def oneColumnCount(rs: ResultSet) = 1
  private def regularStatement(conn: Connection) = conn.createStatement()
  private def flatStatement(conn: Connection) = {
    val st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
    st.setFetchSize(0)
    st
  }
  private def rsToArray(rs: ResultSet, columnCount: Int) = Range(1, columnCount+1).map(i => rs.getObject(i)).toArray

}
  
  
/**
 * Simple no-operation implementation of DataSource interface
 */
class DBClientDataSource (driver: String, url: String, user: String, password: String) extends DataSource {
  
  Class.forName(driver);
  
  override def getConnection(u: String, p: String) = DriverManager.getConnection(url, u, p)
  override def getConnection() = getConnection(user, password)
  override def setLogWriter(out: PrintWriter) = ???
  override def getLogWriter() = ???
  override def setLoginTimeout(seconds: Int) = ???
  override def getLoginTimeout() = ???
  override def unwrap[T](iface: Class[T]) = ???
  override def isWrapperFor(iface: Class[_]) = ???
  override def getParentLogger() = ???  
}