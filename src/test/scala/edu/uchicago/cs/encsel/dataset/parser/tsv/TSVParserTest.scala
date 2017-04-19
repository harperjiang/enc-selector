package edu.uchicago.cs.encsel.dataset.parser.tsv

import org.junit.Assert._
import org.junit.Test
import java.io.File

import edu.uchicago.cs.encsel.dataset.schema.Schema

class TSVParserTest {

  @Test
  def testParseLine: Unit = {
    val parser = new TSVParser
    parser.schema = new Schema()
    val result = parser.parseLine("a\t\tb\tttt\t\tdae_ma\tafsew\t\t")

    assertEquals(9, result.length())
  }

  @Test
  def testGuessHeader: Unit = {
    val parser = new TSVParser
    val records = parser.parse(new File("src/test/resource/test_tsv_parser.tsv").toURI, null).toArray
    assertArrayEquals(Array[Object]("M", "W", "N", "O"), parser.guessHeaderName.toArray[Object])
    assertEquals(5, records.length)
    assertEquals(4, records(0).length())
    assertEquals("3$$3.3$$Good Dog2$$7", records(2).toString())
    assertEquals("4,3.4,Good Dog3,8", records(3).iterator().mkString(","))
  }
}