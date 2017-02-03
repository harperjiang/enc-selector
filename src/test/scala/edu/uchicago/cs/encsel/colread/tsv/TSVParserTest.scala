package edu.uchicago.cs.encsel.colread.tsv

import org.junit.Test
import org.junit.Assert._

class TSVParserTest {

  @Test
  def testParseLine: Unit = {
    var parser = new TSVParser
    var result = parser.parseLine("a\t\tb\tttt\tdae_ma\tafsew")
    
    assertEquals(6, result.length)
  }
}