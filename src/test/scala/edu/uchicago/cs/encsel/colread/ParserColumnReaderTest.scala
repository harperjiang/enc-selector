package edu.uchicago.cs.encsel.colread

import java.io.File

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.colread.csv.CSVColumnReader
import scala.io.Source
import edu.uchicago.cs.encsel.colread.json.JsonColumnReader

class ParserColumnReaderTest {

  @Test
  def testParseColumnReader1: Unit = {
    var schema = Schema.fromParquetFile(new File("resource/test_colreader.schema").toURI())

    var cr = new CSVColumnReader
    var cols = cr.readColumn(new File("resource/test_colreader.csv").toURI(), schema)

    assertEquals(5, cols.size)

    cols.foreach(col => {
      assertEquals(1, Source.fromFile(col.colFile).getLines().size)
    })

    schema.hasHeader = false
    cols = cr.readColumn(new File("resource/test_colreader.csv").toURI(), schema)

    assertEquals(5, cols.size)

    cols.foreach(col => {
      assertEquals(2, Source.fromFile(col.colFile).getLines().size)
    })
  }

  @Test
  def testParseColumnReader2: Unit = {
    var schema = Schema.fromParquetFile(new File("resource/test_json_parser.schema").toURI())

    var cr = new JsonColumnReader
    var cols = cr.readColumn(new File("resource/test_json_parser.json").toURI(), schema)

    assertEquals(3, cols.size)

    cols.foreach(col => {
      assertEquals(5, Source.fromFile(col.colFile).getLines().size)
    })

  }
}