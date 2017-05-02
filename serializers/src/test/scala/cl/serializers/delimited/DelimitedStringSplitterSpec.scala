package cl.serializers.delimited

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.FlatSpec
import org.scalatest.GivenWhenThen

import DelimitedStringSplitter._

/**
 * Specification for delimited string splitter.
 */
@RunWith(classOf[org.scalatest.junit.JUnitRunner])
class DelimitedStringSplitterSpec extends FlatSpec with Matchers with GivenWhenThen {
  
  behavior of "delimited string splitter"

  Given("a delimited string with unquoted values")
  it should "simply split it" in {
    c("1997,Ford,E350") should equal(Array("1997", "Ford", "E350"))
    p("1997|Ford|E350") should equal(Array("1997", "Ford", "E350"))
  }
  
  Given("a delimited string with quoted values")
  it should "split it and discard enclosers" in {
    c("\"1997\",\"Ford\",\"E350\"") should equal(Array("1997", "Ford", "E350"))
    p("\"1997\"|\"Ford\"|\"E350\"") should equal(Array("1997", "Ford", "E350"))
  }
  
  Given("a delimited string with enclosed values containing the delimiter")
  it should "ignore the enclosed delimiters and discard enclosers" in {
    c("1997,Ford,E350,\"Super, luxurious truck\"") should equal(Array("1997", "Ford", "E350", "Super, luxurious truck"))
    p("1997|Ford|E350|\"Super| luxurious truck\"") should equal(Array("1997", "Ford", "E350", "Super| luxurious truck"))
  }
  
  Given("a delimited string with values containing escaped (double) delimiters")
  it should "represent them as regular characters" in {
    c("1997,Ford,E350,\"Super, \"\"luxurious\"\" truck\"") should equal(Array("1997", "Ford", "E350", "Super, \"luxurious\" truck"))
    p("1997|Ford|E350|\"Super| \"\"luxurious\"\" truck\"") should equal(Array("1997", "Ford", "E350", "Super| \"luxurious\" truck"))

    c("1997,Ford,E350,\"Super, \"\"\"luxurious\"\"\" truck\"") should equal(Array("1997", "Ford", "E350", "Super, \"\"luxurious\"\" truck"))
    p("1997|Ford|E350|\"Super| \"\"\"luxurious\"\"\" truck\"") should equal(Array("1997", "Ford", "E350", "Super| \"\"luxurious\"\" truck"))
    
    c("1997,Ford,E350,\"Super, \"\"\"\"luxurious\"\"\"\" truck\"") should equal(Array("1997", "Ford", "E350", "Super, \"\"luxurious\"\" truck"))
    p("1997|Ford|E350|\"Super| \"\"\"\"luxurious\"\"\"\" truck\"") should equal(Array("1997", "Ford", "E350", "Super| \"\"luxurious\"\" truck"))
    
    c("1997,Ford,E350,\"\"Super, luxurious\"\" truck\"") should equal(Array("1997", "Ford", "E350", "\"Super, luxurious\" truck"))
    p("1997|Ford|E350|\"\"Super| luxurious\"\" truck\"") should equal(Array("1997", "Ford", "E350", "\"Super| luxurious\" truck"))
  }
  
  Given("a delimited string with enclosed or unenclosed new line characters")
  it should "treat new line characters as usual characters" in {
    c("1997,Ford,E350,\"Go get one now" + System.lineSeparator() +  "they are going fast\",sold") should equal(
        Array("1997", "Ford", "E350", "Go get one now" + System.lineSeparator() +  "they are going fast", "sold"))
    p("1997|Ford|E350|Go get one now" + System.lineSeparator() +  "they are going fast|sold") should equal(
        Array("1997", "Ford", "E350", "Go get one now" + System.lineSeparator() +  "they are going fast", "sold"))
  }
  
  Given("a delimited string with enclosed leading and/or trailing whitespace characters")
  And("when 'trim' is set to false")
  it should "leave these whitespace characters respecting trim == false" in {
    c("1997,\"   Ford\t  \",E350") should equal(Array("1997", "   Ford\t  ", "E350"))
    p("1997|\"   Ford\t  \"|E350") should equal(Array("1997", "   Ford\t  ", "E350"))
  }
  
  Given("a delimited string with enclosed leading and/or trailing whitespace characters")
  And("when 'trim' is set to true")
  it should "leave these whitespace characters ignoring trim == false" in {
    ctrim("1997,\"   Ford\t  \",E350") should equal(Array("1997", "   Ford\t  ", "E350"))
    ptrim("1997|\"   Ford\t  \"|E350") should equal(Array("1997", "   Ford\t  ", "E350"))
  }
  
  Given("a delimited string with un-enclosed leading and/or trailing whitespace characters")
  And("when 'trim' is set to false")
  it should "leave these whitespace characters in the output repsecting trim == false" in {
    c("1997,   Ford\t  ,E350") should equal(Array("1997", "   Ford\t  ", "E350"))
    p("1997|   Ford\t  |E350") should equal(Array("1997", "   Ford\t  ", "E350"))
  }
  
  Given("a delimited string with un-enclosed leading and/or trailing whitespace characters")
  And("when 'trim' is set to true")
  it should "remove these whitespace characters from the output repsecting trim == true" in {
    ctrim("1997,   Ford\t  ,E350") should equal(Array("1997", "Ford", "E350"))
    ptrim("1997|   Ford\t  |E350") should equal(Array("1997", "Ford", "E350"))
  }

  Given("a null value") 
  it should "return null" in {
    c(null) should equal(null)
    p(null) should equal(null)
  }
  
  Given("an empty string")
  it should "return an array of size one with empty string" in {
    c("") should equal(Array(""))
    p("") should equal(Array(""))    
  }
  
  Given("a string which contains delimiters only")
  it should "return an array of empty strings of size one greater than the number of delimiter in the input" in {
    c(",,") should equal(Array("", "", ""))
    p("||") should equal(Array("", "", ""))
  }
  
  Given("a string in which the last character is an unmatched encloser")
  it should "treat that encloser as a regular character" in {
    c("One,Two\"") should equal(Array("One", "Two\""))
    p("One|Two\"") should equal(Array("One", "Two\""))
  }
  
  Given("a string in which the last character is an unmatched encloser right after the delimiter")
  it should "treat that encloser as a regular character and output it" in {
    c("One,\"") should equal(Array("One", "\""))
    p("One|\"") should equal(Array("One", "\""))
  }
  
  private def c(s: String) = DelimitedStringSplitter.csv().split(s)
  private def p(s: String) = DelimitedStringSplitter.pipe().split(s)
  private def ctrim(s: String) = DelimitedStringSplitter.csvTrimming.split(s)
  private def ptrim(s: String) = DelimitedStringSplitter.pipeTrimming().split(s)
}