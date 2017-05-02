package cl.serializers.delimited

import org.scalatest.Matchers
import org.scalatest.FlatSpec

import cl.serializers.delimited.DelimitedStringJoiner._
import org.junit.runner.RunWith
import cl.core.configurable.ConfigurableException

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
  
  it should "check for new  lines and convert them to spaces when instructed to do so" in {
    val newLine = System.lineSeparator()
    cNewLine(1,"Line " + newLine + " interrupted",3) should be ("1,Line   interrupted,3")
    pNewLine(1,"Line " + newLine + " interrupted",3) should be ("1|Line   interrupted|3")
    tNewLine(1,"Line " + newLine + " interrupted",3) should be ("1\tLine   interrupted\t3")
    sNewLine(1,"Line " + newLine + " interrupted",3) should be ("1 \"Line   interrupted\" 3")
  }
  
  it must "throw an exception when used while unlocked" in {
    a [ConfigurableException] should be thrownBy get().join("one")
  }
  
  private def join(joiner: DelimitedStringJoiner, values: Any*) = joiner.join(values.map(_.asInstanceOf[AnyRef]) : _*)
  
  private def c(values: Any*) = join(csv(),   values : _*)
  private def p(values: Any*) = join(pipe(),  values : _*)
  private def t(values: Any*) = join(tab(),   values : _*)
  private def s(values: Any*) = join(space(), values : _*)
  
  private def cEnc(values: Any*) = join(csvEnclosing(),   values : _*)
  private def pEnc(values: Any*) = join(pipeEnclosing(),  values : _*)
  private def tEnc(values: Any*) = join(tabEnclosing(),   values : _*)
  private def sEnc(values: Any*) = join(spaceEnclosing(), values : _*)
  
  private def cTrim(values: Any*) = join(csvTrimming(),   values : _*)
  private def pTrim(values: Any*) = join(pipeTrimming(),  values : _*)
  private def tTrim(values: Any*) = join(tabTrimming(),   values : _*)
  private def sTrim(values: Any*) = join(spaceTrimming(), values : _*)
  
  private def cTrimEnc(values: Any*) = join(csvTrimmingAndEnclosing(),   values : _*)
  private def pTrimEnc(values: Any*) = join(pipeTrimmingAndEnclosing(),  values : _*)
  private def tTrimEnc(values: Any*) = join(tabTrimmingAndEnclosing(),   values : _*)
  private def sTrimEnc(values: Any*) = join(spaceTrimmingAndEnclosing(), values : _*)
  
  private def nlChecker(joiner: DelimitedStringJoiner) = 
    joiner.`with`(DelimitedStringJoiner.checkForNewLines, java.lang.Boolean TRUE).locked()
  
  private def cNewLine(values: Any*) = join(nlChecker(csv(false)),   values : _*)
  private def pNewLine(values: Any*) = join(nlChecker(pipe(false)),  values : _*)
  private def tNewLine(values: Any*) = join(nlChecker(tab(false)),   values : _*)
  private def sNewLine(values: Any*) = join(nlChecker(space(false)), values : _*)
}