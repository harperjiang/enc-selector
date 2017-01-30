package edu.uchicago.cs.encsel.colread.json

import java.io.File

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.colread.Schema

class JsonParserTest {

  @Test
  def testParse: Unit = {
    var parser = new JsonParser
    var schema = Schema.fromParquetFile(new File("resource/test_json_parser.schema").toURI())

    parser.schema = schema
    var data = parser.parseLine("""{"id":32,"name":"WangDaChui","gender":"male","rpg":"Good"}""")
    assertEquals(3, data.length)
    
  }
}