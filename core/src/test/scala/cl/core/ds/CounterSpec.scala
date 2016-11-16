package cl.core.ds

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers

/**
 * Counter specification
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class CounterSpec extends FlatSpec with Matchers {
  
  behavior of "a Counter"
  
  it should "correctly set its initial value" in {
    new Counter().get should be (0)
    new Counter(-987654321).get should be (-987654321)
  }
  
  it should "increment its value after calling increment()" in {
    val c = new Counter;
    c.increment() 
    c.get should be (1)
  }
  
  it should "change its value after calling add()" in {
    val c = new Counter
    c.add(-10)
    c.get should be (-10)
  }
  
  it should "reset to its initial value" in {
    val c1 = new Counter
    c1.add(10)
    c1.reset() 
    c1.get should be (0)
    
    val c2 = new Counter(555)
    c2.add(10)
    c2.reset()
    c2.get should be (555)
  }
  
  it can "increment and get the value in one method call" in {
    val c = new Counter
    val value = c.incrementAndGet()
    value should be (1)
  }
  
  it can "get and increment the value in one method call" in {
    val c = new Counter(7)
    val value = c.getAndIncrement
    value should be (7)
    c.get should be (8)
  }
  
  it can "get the value and reset in one method call" in {
    val c = new Counter(7)
    c.add(10)
    val value = c.getAndReset()
    value should be (17)
    c.get should be (7)
  }

}