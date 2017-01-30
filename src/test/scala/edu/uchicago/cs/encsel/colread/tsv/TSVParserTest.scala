package edu.uchicago.cs.encsel.colread.tsv

import org.junit.Test
import org.junit.Assert._

class TSVParserTest {

  @Test
  def testParseLine: Unit = {
    var parser = new TSVParser
    var result = parser.parseLine("a b	ttt	dae_ma	afsew")
    
    assertEquals(5, result.length)
  }
}