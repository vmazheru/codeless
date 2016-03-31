package cl.core.util

import org.junit.runner.RunWith
import org.scalatest.FunSuite

import cl.core.function.ScalaToJava._

@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class ThreadsTest extends FunSuite {
  
  test("Thread sleep interruption") {
    val sleepTime = 5000
    val t = new Thread(() => Threads.sleep(sleepTime))
    val start = System.currentTimeMillis()
    t.start()
    t.interrupt()
    t.join()
    val end = System.currentTimeMillis()
    assert((end - start) < sleepTime / 2 ,
        "Execution time must be much less than sleep time, " +
        "since the thread has been interrupted while sleeping")
  }
  
}