package edu.uchicago.cs.encsel.parser.tsv

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.parser.tsv.TSVParser;

class TSVParserTest {

  @Test
  def testParseLine: Unit = {
    var parser = new TSVParser
    var result = parser.parseLine("a\t\tb\tttt\tdae_ma\tafsew")
    
    assertEquals(6, result.length)
  }
}