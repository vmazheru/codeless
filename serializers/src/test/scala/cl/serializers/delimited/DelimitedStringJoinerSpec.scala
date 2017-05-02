package cl.serializers.delimited

import org.scalatest.Matchers
import org.scalatest.FlatSpec

import cl.serializers.delimited.DelimitedStringJoiner._
import org.junit.runner.RunWith

/**
 * Specification for delimited string joiner.
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DelimitedStringJoinerSpec extends FlatSpec with Matchers {
  
  behavior of "delimited string joiner"
  
  it should "represent null objects as empty strings" in {
    c(null, null) should be (",")
    p(null, null) should be ("|")
    t(null, null) should be ("\t")
    s(null, null) should be (" ")
    
    c(null) should be ("")
    p(null) should be ("")
    t(null) should be ("")
    s(null) should be ("")
  }
  
  it should "join values with the configured delimiter" in {
    c(1,2,3) should be ("1,2,3")
    p(1,2,3) should be ("1|2|3")
    t(1,2,3) should be ("1\t2\t3")
    s(1,2,3) should be ("1 2 3")
  }
  
  it should "enclose values which contain delimiters" in {
    c(1,"Line, interrupted",3) should be ("1,\"Line, interrupted\",3")
    p(1,"Line| interrupted",3) should be ("1|\"Line| interrupted\"|3")
    t(1,"Line\t interrupted",3) should be ("1\t\"Line\t interrupted\"\t3")
    s(1,"Line interrupted",3) should be ("1 \"Line interrupted\" 3")
  }
  
  it should "enclose values which contain leading and/or trailng spaces" in {
    c("  1","2  ","  3   ",4) should be ("\"  1\",\"2  \",\"  3   \",4")
    p("  1","2  ","  3   ",4) should be ("\"  1\"|\"2  \"|\"  3   \"|4")
    t("  1","2  ","  3   ",4) should be ("\"  1\"\t\"2  \"\t\"  3   \"\t4")
    s("  1","2  ","  3   ",4) should be ("\"  1\" \"2  \" \"  3   \" 4")
  }
  
  it should "enclose values which contain enclosers and escape the enclosers" in {
    c("Number \"one\"",2,"Number \"three\"","Number \"four\" is last") should be (
        "\"Number \"\"one\"\"\",2,\"Number \"\"three\"\"\",\"Number \"\"four\"\" is last\"")
    p("Number \"one\"",2,"Number \"three\"","Number \"four\" is last") should be (
        "\"Number \"\"one\"\"\"|2|\"Number \"\"three\"\"\"|\"Number \"\"four\"\" is last\"")
    t("Number \"one\"",2,"Number \"three\"","Number \"four\" is last") should be (
        "\"Number \"\"one\"\"\"\t2\t\"Number \"\"three\"\"\"\t\"Number \"\"four\"\" is last\"")
    s("Number \"one\"",2,"Number \"three\"","Number \"four\" is last") should be (
        "\"Number \"\"one\"\"\" 2 \"Number \"\"three\"\"\" \"Number \"\"four\"\" is last\"")
  }
  
  it should "always enclose values when instructed to do so" in {
    cEnc(1,"Line, interrupted",3) should be ("\"1\",\"Line, interrupted\",\"3\"")
    pEnc(1,"Line| interrupted",3) should be ("\"1\"|\"Line| interrupted\"|\"3\"")
    tEnc(1,"Line\t interrupted",3) should be ("\"1\"\t\"Line\t interrupted\"\t\"3\"")
    sEnc(1,"Line interrupted",3) should be ("\"1\" \"Line interrupted\" \"3\"")
  }
  
  it should "trim values when instructed to do so" in {
    cTrim("  1","2  ","  3   ",4) should be ("1,2,3,4")
    pTrim("  1","2  ","  3   ",4) should be ("1|2|3|4")
    tTrim("\t  1","2 \t ","  3  \t ",4) should be ("1\t2\t3\t4")
    sTrim("  1","2  ","  3   ",4) should be ("1 2 3 4")
  }
  
  it should "trim and enclose values when instructed to do so" in {
    cTrimEnc("  1","2  ","  3   ",4) should be ("\"1\",\"2\",\"3\",\"4\"")
    pTrimEnc("  1","2  ","  3   ",4) should be ("\"1\"|\"2\"|\"3\"|\"4\"")
    tTrimEnc("\t  1","2 \t ","  3  \t ",4) should be ("\"1\"\t\"2\"\t\"3\"\t\"4\"")
    sTrimEnc("  1","2  ","  3   ",4) should be ("\"1\" \"2\" \"3\" \"4\"")
  }
  
  
  private def c(values: Any*) = csv().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def p(values: Any*) = pipe().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def t(values: Any*) = tab().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def s(values: Any*) = space().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  
  private def cEnc(values: Any*) = csvEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def pEnc(values: Any*) = pipeEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def tEnc(values: Any*) = tabEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def sEnc(values: Any*) = spaceEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  
  private def cTrim(values: Any*) = csvTrimming().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def pTrim(values: Any*) = pipeTrimming().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def tTrim(values: Any*) = tabTrimming().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def sTrim(values: Any*) = spaceTrimming().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  
  private def cTrimEnc(values: Any*) = csvTrimmingAndEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def pTrimEnc(values: Any*) = pipeTrimmingAndEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def tTrimEnc(values: Any*) = tabTrimmingAndEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
  private def sTrimEnc(values: Any*) = spaceTrimmingAndEnclosing().join(values.map(_.asInstanceOf[AnyRef]) : _*)
}