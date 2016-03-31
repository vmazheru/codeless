package cl.core.configurable

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen

/**
 * A specification for {@code ConfigurableObject} class.
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class ConfigurableObjectSpec extends FlatSpec with Matchers with GivenWhenThen {

  behavior of "a configurable object"
  
  Given("Some useful class with several properties including a property of a custom type as")
  class CustomKeyType (val name: String)
  class UsefulClass extends ConfigurableObject[UsefulClass]
  object UsefulClass {
    val booleanKey = new Key[Boolean]{}
    val stringKey  = new Key[String]{}
    val intKey     = new Key[Integer]{}
    val customKey  = new Key[CustomKeyType]{}
  }
  import UsefulClass._
  
  it should "retain values of properties set" in {
    
    When("properties are set")
    
    val c = new UsefulClass()
      .`with`(booleanKey, true)
      .`with`(stringKey, "test")
      .`with`(intKey, new Integer(5))
      .`with`(customKey, new CustomKeyType("foo"))
      .locked
    
    Then("they should be accessible via get()")
    
    c.get(booleanKey)     should be (true)
    c.get(stringKey)      should be ("test")
    c.get(intKey)         should be (new Integer(5))
    c.get(customKey).name should be ("foo")
  
  }
  
  it should "not allow setting properties after being locked" in {
    val c = new UsefulClass().`with`(booleanKey, true).locked()
    intercept[ConfigurableException] {
      c.`with`(intKey, new Integer(1))
    }
  }

}