package edu.uchicago.cs.encsel.parser.json

import java.io.File

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

import edu.uchicago.cs.encsel.schema.Schema

class JsonParserTest {

  @Test
  def testParse: Unit = {
    var parser = new LineJsonParser
    var schema = Schema.fromParquetFile(new File("src/test/resource/test_json_parser.schema").toURI())

    parser.schema = schema
    var data = parser.parseLine("""{"id":32,"name":"WangDaChui","gender":"male","rpg":"Good"}""")
    assertEquals(3, data.length)
  }

  @Test
  def testGuessHeader: Unit = {
    var parser = new LineJsonParser
    var records = parser.parse(new File("src/test/resource/test_json_parser.json").toURI(), null).toArray
    assertArrayEquals(Array[Object]("add", "gender", "id", "name"), parser.guessHeaderName().toArray[Object])
    assertEquals(5, records.size)
    assertEquals(4, records(0).length())
    assertEquals("""$$male$$34$${"a":"x","b":"c"}""", records(2).toString())
    assertEquals(",female,35,Lily3", records(3).iterator().mkString(","))
  }
}