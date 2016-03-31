package cl.core.ds

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers

/**
 * Pair specification
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class PairSpec extends FlatSpec with Matchers {
  
  behavior of "a Pair" 
  val p = new Pair(1, "Hello")
    
  it should "correctly return its first element"  in { p._1 should be (1) }
  it should "correctly return its second element" in { p._2 should be ("Hello") }
  
}