package edu.uchicago.cs.encsel.parser.json

import java.io.File
import org.junit.Test
import org.junit.Assert._
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
}