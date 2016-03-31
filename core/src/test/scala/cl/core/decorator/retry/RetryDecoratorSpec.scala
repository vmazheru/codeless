package cl.core.decorator.retry

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen
import org.scalatest.Matchers
import java.io.IOException
import java.io.FileNotFoundException
import java.util.function.Supplier
import cl.core.ds.Counter
import cl.core.function.ScalaToJava._
import java.util.function.Consumer
import java.util.function.BiConsumer
import java.util.function.BiFunction

/**
 * Retry decorator spec
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class RetryDecoratorSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "retry decorator"
  
  Given ("a simple retry policy which runs for 3 times with sleep time of 10 ms")
  def rp = new SimpleRetryPolicy(3, 10)
  
  Given ("runnables throwing different types of exceptions")
  val runnableWithRuntimeException     : Runnable = () => throw new RuntimeException("foo")
  val runnableWithNullPointerException : Runnable = () => throw new NullPointerException("foo")
  val runnableWithFileNotFoundException: Runnable = () => throw new FileNotFoundException("foo")


  it should "execute code 3 times in case of error" in {
    val counter = new Counter()
    val d = new RetryDecorator(rp, null, null, () => counter.increment())
    intercept[RuntimeException] {
      d.decorate(runnableWithRuntimeException).run()
    }
    counter.getValue should be (rp.getNumRetries)
  }
  
  
  it should "execute no faster than sleepTime * numberOfRetries" in {
    val d = new RetryDecorator(rp, null, null, null)
    val retriedRunnable = d.decorate(runnableWithRuntimeException)
    
    val start = System.currentTimeMillis()
    intercept[RuntimeException] {
      retriedRunnable.run()
    }
    val timeSpent = System.currentTimeMillis() - start
    val minimalExpectedRunningTime = rp.getNumRetries * rp.getSleepTime

    timeSpent should be >= minimalExpectedRunningTime
  }
  
  
  it should "not process exceptions which are not in the list of expected exceptions" in {
    val counter = new Counter()
    val d = new RetryDecorator(rp,
        Array(classOf[IOException], classOf[NullPointerException]), null, () => counter.increment())
    intercept[RuntimeException] {
      d.decorate(runnableWithRuntimeException).run()
    }
    counter.getValue should be (0)
  }
  
  
  it should "process exceptions which are in the list of expected exceptions" in {
    val counter = new Counter()
    val d = new RetryDecorator(rp,
        Array(classOf[IOException], classOf[NullPointerException]), null, () => counter.increment())
    intercept[RuntimeException] {
      d.decorate(runnableWithNullPointerException).run()
    }
    counter.getValue should be (rp.getNumRetries)
  }
  
  
  it should "process exceptions which are sub-types of the exceptions in the list" in {
    val counter = new Counter()
    val d = new RetryDecorator(rp,
        Array(classOf[IOException], classOf[NullPointerException]), null, () => counter.increment())
    intercept[FileNotFoundException] {
      d.decorate(runnableWithFileNotFoundException).run()
    }
    counter.getValue should be (rp.getNumRetries)
  }
  
  
  it should "call its callbacks" in {
    val beforeCounter = new Counter()
    val afterCounter = new Counter()
    val d = new RetryDecorator(rp,
        null , (ex: Exception) => beforeCounter.increment(), () => afterCounter.increment())
    intercept[RuntimeException] {
      d.decorate(runnableWithRuntimeException).run()
    }
    
    beforeCounter.getValue should be (rp.getNumRetries)
    afterCounter.getValue should be (rp.getNumRetries)
  }
  
  
  it should "not fail when expected exceptions are not given" in {
    val d = new RetryDecorator(rp, null, (ex: Exception) => {}, () => {})
    val ex = intercept[RuntimeException] {
      d.decorate(runnableWithRuntimeException).run()
    }
    ex.getMessage should be ("foo")
  }
  
  
  it should "not fail when 'before' callback is not given" in {
    val d = new RetryDecorator(rp, Array(classOf[RuntimeException]), null, () => {})
    val ex = intercept[RuntimeException] {
      d.decorate(runnableWithRuntimeException).run()
    }
    ex.getMessage should be ("foo")
  }

  
  it should "not fail when 'after' callback is not given" in {
    val d = new RetryDecorator(rp, Array(classOf[RuntimeException]), (ex: Exception) => {}, null)
    val ex = intercept[RuntimeException] {
      d.decorate(runnableWithRuntimeException).run()
    }
    ex.getMessage should be ("foo")
  }
  
  
  it should "backup static decorating methods" in {
    
    // there is a service which throws ShakyServiceException
    class ShakyServiceException(msg:String) extends RuntimeException(msg)

    // throws an exception on the first call to any function
    object ShakyService {
      private val counter = new Counter

      def run() {
        val isError = counter.getValue == 0
        counter.increment()
        if (isError) {
          throw new ShakyServiceException("foo")
        }
      }
      
      def get = { run(); 0 }
      def accept(i: Int) { run() }
      def accept(i: Int, j: Int) { run() }
      def apply(i: Int) = { run(); i }
      def apply(i: Int, j: Int) = { run(); i + j }
      
      def checkAndReset() {
        counter.getValue should be (2)
        counter.reset()
      }
    }
    
    def test(f: () => Unit) {
      try { 
        f.apply()
        ShakyService.checkAndReset()
      } catch { case _ : ShakyServiceException => fail }
    }
    
    val runnable:   Runnable                  = () => ShakyService.run
    val supplier:   Supplier[Int]             = () => ShakyService.get
    val consumer:   Consumer[Int]             = (i: Int) => ShakyService.accept(i)
    val biConsumer: BiConsumer[Int, Int]      = (i: Int, j: Int) => ShakyService.accept(i,j)
    val function:   java.util.function.Function[Int, Int]        = (i: Int) => ShakyService.apply(i)
    val biFunction: BiFunction[Int, Int, Int] = (i: Int, j:Int) => ShakyService.apply(i,j)

    val exceptions: Array[Class[_ <: Exception]] = Array(classOf[ShakyServiceException])
    
    import RetryDecorators._

    // test Runnable
    test(() => retried(3, 10, runnable).run)
    test(() => retry  (3, 10, runnable))
    
    test(() => retried(rp, runnable).run)
    test(() => retry  (rp, runnable))
    
    test(() => retried(rp, exceptions, runnable).run)
    test(() => retry  (rp, exceptions, runnable))
    
    test(() => retried(rp, (ex: Exception) => {}, runnable).run)
    test(() => retry  (rp, (ex: Exception) => {}, runnable))
    
    test(() => retried(rp, (ex: Exception) => {}, runnable, () => {}).run)
    test(() => retry  (rp, (ex: Exception) => {}, runnable, () => {}))

    test(() => retried(rp, exceptions, (ex: Exception) => {}, runnable).run)
    test(() => retry  (rp, exceptions, (ex: Exception) => {}, runnable))

    test(() => retried(rp, exceptions, (ex: Exception) => {}, runnable, () => {}).run)
    test(() => retry  (rp, exceptions, (ex: Exception) => {}, runnable, () => {}))

    // test Supplier
    test(() => retried(3, 10, supplier).get)
    test(() => retry  (3, 10, supplier))    

    test(() => retried(rp, supplier).get)
    test(() => retry  (rp, supplier))
    
    test(() => retried(rp, exceptions, supplier).get)
    test(() => retry  (rp, exceptions, supplier))
    
    test(() => retried(rp, (ex: Exception) => {}, supplier).get)
    test(() => retry  (rp, (ex: Exception) => {}, supplier))
    
    test(() => retried(rp, (ex: Exception) => {}, supplier, () => {}).get)
    test(() => retry  (rp, (ex: Exception) => {}, supplier, () => {}))

    test(() => retried(rp, exceptions, (ex: Exception) => {}, supplier).get)
    test(() => retry  (rp, exceptions, (ex: Exception) => {}, supplier))

    test(() => retried(rp, exceptions, (ex: Exception) => {}, supplier, () => {}).get)
    test(() => retry  (rp, exceptions, (ex: Exception) => {}, supplier, () => {}))
    
    // test Consumer
    test(() => retried(3, 10, consumer).accept(1))
    test(() => retried(rp, consumer).accept(1))
    test(() => retried(rp, exceptions, consumer).accept(1))
    test(() => retried(rp, (ex: Exception) => {}, consumer).accept(1))
    test(() => retried(rp, (ex: Exception) => {}, consumer, () => {}).accept(1))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, consumer).accept(1))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, consumer, () => {}).accept(1))
    
    // test BiConsumer
    test(() => retried(3, 10, biConsumer).accept(1,2))
    test(() => retried(rp, biConsumer).accept(1,2))
    test(() => retried(rp, exceptions, biConsumer).accept(1,2))
    test(() => retried(rp, (ex: Exception) => {}, biConsumer).accept(1,2))
    test(() => retried(rp, (ex: Exception) => {}, biConsumer, () => {}).accept(1,2))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, biConsumer).accept(1,2))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, biConsumer, () => {}).accept(1,2))
    
    // test Function
    test(() => retried(3, 10, function).apply(1))
    test(() => retried(rp, function).apply(1))
    test(() => retried(rp, exceptions, function).apply(1))
    test(() => retried(rp, (ex: Exception) => {}, function).apply(1))
    test(() => retried(rp, (ex: Exception) => {}, function, () => {}).apply(1))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, function).apply(1))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, function, () => {}).apply(1))
    
    // test BiFunction
    test(() => retried(3, 10, biFunction).apply(1,2))
    test(() => retried(rp, biFunction).apply(1,2))
    test(() => retried(rp, exceptions, biFunction).apply(1,2))
    test(() => retried(rp, (ex: Exception) => {}, biFunction).apply(1,2))
    test(() => retried(rp, (ex: Exception) => {}, biFunction, () => {}).apply(1,2))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, biFunction).apply(1,2))
    test(() => retried(rp, exceptions, (ex: Exception) => {}, biFunction, () => {}).apply(1,2))
  }
  
}