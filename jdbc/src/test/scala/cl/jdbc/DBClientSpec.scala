package cl.jdbc

import java.io.PrintWriter
import java.nio.file.Files
import java.util.Arrays
import scala.beans.BeanProperty
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import cl.core.function.ScalaToJava._
import cl.core.lang.Control._
import cl.jdbc.DBClient.DBClientException
import java.sql.ResultSet

/**
 * DBClient specification
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DBClientSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "a DB client"
  
  Given("that a DB client points to an empty test DB")
  val db = new SQLiteDB(Files.createTempFile("db", null))
  val client = db.getClient

  try {

    it can "run DDL and DML statements" in {
      try {
        client.openConnection()
        client.update("CREATE TABLE test (id, text)")
        client.update("INSERT INTO test VALUES (1, 'foo')")
        client.update("INSERT INTO test VALUES (2, 'bar')")
        client.update("INSERT INTO test VALUES (3, 'zoo')")
        client.update("UPDATE test SET text = 'baz' WHERE id = 3")
      } finally {
        client.closeConnection()
      }
    }
    
    it can "retrieve rows as arrays of Object's" in {
      val rows = client.getTable("SELECT * FROM test ORDER BY id")
      rows.size() should be (3)
      rows.forEach((row: Array[AnyRef]) => {
        row.length should be (2)
      })
      rows.get(0)(0) should be (1)
      rows.get(0)(1) should be ("foo")
    }
    
    it can "retrieve single-column rows as arrays of objects of some specific type" in {
      val ids: java.util.List[Int] = client.getVector("SELECT id FROM test ORDER BY id")
      ids.size() should be (3)
      ids.get(0) should be (1)
      
      val values: java.util.List[String] = client.getVector("SELECT text FROM test ORDER BY id")
      values.size() should be (3)
      values.get(0) should be ("foo")
    }
    
    it can "convert single-value SELECT result into a scalar value" in {
      val bar: String = client.getScalar("SELECT text FROM test WHERE id = 2")
      bar should be("bar")
      
      val numRows:Int = client.getScalar("SELECT COUNT(*) FROM test")
      numRows should be (3)
    }
    
    it can "process rows as they arrive from result set to lower memory consumption" in {
      val ids = scala.collection.mutable.Set[Int]()
      val onRow: Array[AnyRef] => Unit = row => ids += row(0).asInstanceOf[Int] 
      client.getTable("SELECT * FROM test", onRow)
      ids should be (Set(1,2,3))
      
      val values = scala.collection.mutable.Set[String]()
      val onValue: String => Unit = v => values += v
      client.getVector("SELECT text FROM test", onValue)
      values should be (Set("foo", "bar", "baz"))
    }
    
    it can "map the result set to objects of some specific type by using reflection" in {
      val records = client.getObjects("SELECT id, text AS keyword FROM test ORDER BY id", classOf[JavaTestRecord])
      records.size should be (3)
      records.get(0).id should be (1)
      records.get(0).keyword should be ("foo")
      
      val ids = scala.collection.mutable.Set[Int]()
      val values = scala.collection.mutable.Set[String]()
      val onObject: JavaTestRecord => Unit = record => {
            ids += record.getId()
            values += record.getKeyword()
          } 
      client.getObjects("SELECT id, text AS keyword FROM test ORDER BY id", classOf[JavaTestRecord], onObject)
      ids should be (Set(1,2,3))
      values should be (Set("foo", "bar", "baz"))
    }
    
    it can "map the result set to objects of some specific type by using a mapping function" in {
      val mapper: ResultSet => ScalaTestRecord = rs => new ScalaTestRecord(rs.getInt(1), rs.getString(2))
      val records = client.getObjects("SELECT * FROM test", mapper)
      records.size should be (3)
      records.get(0).id should be (1)
      records.get(0).keyword should be ("foo")

      val ids = scala.collection.mutable.Set[Int]()
      val values = scala.collection.mutable.Set[String]()
      val onObject: ScalaTestRecord => Unit = record => {
            ids += record.id
            values += record.keyword
          } 
      client.getObjects("SELECT id, text AS keyword FROM test ORDER BY id", mapper, onObject)
      ids should be (Set(1,2,3))
      values should be (Set("foo", "bar", "baz"))
    }
    
    it can "dump the rows to a print writer" in {
      val f = Files.createTempFile("tmp", "txt")
      try {
        using(new PrintWriter(f.toFile())) { pw =>
          client.dump("SELECT * FROM test ORDER BY id", "|", pw)
        }
        Files.readAllLines(f) should be (Arrays.asList("1|foo", "2|bar", "3|baz"))
      } finally {
        Files.deleteIfExists(f)
      }
    }
    
    it should "throw run-time DB client exception when there is a problem" in {
      intercept[DBClientException] { client.getTable("SELECT * FROM wrong_table") }
      intercept[DBClientException] { client.getVector("SELECT * FROM wrong_table") }
      intercept[DBClientException] { client.getScalar("SELECT COUNT(*) FROM wrong_table") }
      intercept[DBClientException] { client.getTable("SELECT * FROM wrong_table", null) }
      intercept[DBClientException] { client.getVector("SELECT * FROM wrong_table", null) }
    }
    
  } finally {
    db.delete()
  }
}

/*
 * This is a Java bean to which the result set will reflectively be mapped. 
 */
class JavaTestRecord (@BeanProperty var id: Integer, @BeanProperty var keyword: String) {
 def this() {
   this (null.asInstanceOf[Integer], null)
 }
}

/*
 * This is some other type to which the result set will be mapped by using a mapping function.
 */
class ScalaTestRecord (val id: Integer, val keyword: String)
