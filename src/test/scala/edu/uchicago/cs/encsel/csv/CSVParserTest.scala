package edu.uchicago.cs.encsel.csv

import org.junit.Test
import org.junit.Assert._

class CSVParserTest {

  @Test
  def testParseLine(): Unit = {

    var parser = new CSVParser()

    var input = "a,b,c,d,e";
    var output = parser.parseLine(input)

    assertEquals(5, output.length)

    input = "a,3,7,\"323,m4,2,34\""
    output = parser.parseLine(input)

    assertEquals(4, output.length)
  }
}