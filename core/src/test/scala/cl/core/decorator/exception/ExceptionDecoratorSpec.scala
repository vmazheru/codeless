package cl.core.decorator.exception

import java.io.IOException
import java.io.UncheckedIOException

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers

import cl.core.function.ScalaToJava._
import cl.core.decorator.exception.ExceptionDecorators._

/**
 * Exception decorator specification.
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class ExceptionDecoratorSpec extends FlatSpec with Matchers {
  
  behavior of "exception decorator"
  
  it should "wrap a checked exception thrown by the function into a run time exception" in {
    interceptAndCheck(expected = classOf[RuntimeException], cause = classOf[Exception], "Bad!") {
      uncheck(() => throw new Exception("Bad!"))
    }
  }
  
  it should "wrap an IO exception throw by the function into an unchecked IO exception" in {
    interceptAndCheck(classOf[UncheckedIOException], classOf[IOException], "Where is my file?") {
      uncheck(() => throw new IOException("Where is my file?"))
    }
  }
  
  it should "wrap a checked exception thrown by the function into a specific type of run time exception" in {
    interceptAndCheck(classOf[MyException], classOf[Exception], "So bad...") {
      uncheck(classOf[MyException], () => throw new Exception("So bad..."))
    }
  }
  
  private def interceptAndCheck[T,U](expected: Class[T], cause: Class[U], message: String) (f: => Any) {
    val ex = intercept[RuntimeException] { f }
    ex.getClass should be (expected)
    ex.getCause.getClass should be (cause)
    ex.getCause.getMessage should be (message)
  }  
  
}

class MyException (cause: Throwable) extends RuntimeException (cause)