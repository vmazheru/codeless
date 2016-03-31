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
    new Counter().getValue should be (0)
    new Counter(-987654321).getValue should be (-987654321)
  }
  
  it should "increment its value after calling increment()" in {
    val c = new Counter;
    c.increment() 
    c.getValue should be (1)
  }
  
  it should "change its value after calling add()" in {
    val c = new Counter
    c.add(-10)
    c.getValue should be (-10)
  }

}