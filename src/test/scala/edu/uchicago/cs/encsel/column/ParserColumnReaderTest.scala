package edu.uchicago.cs.encsel.column

import java.io.File

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.column.csv.CSVColumnReader
import scala.io.Source
import edu.uchicago.cs.encsel.column.json.JsonColumnReader
import edu.uchicago.cs.encsel.schema.Schema

class ParserColumnReaderTest {

  @Test
  def testParseColumnReader1: Unit = {
    var schema = Schema.fromParquetFile(new File("src/test/resource/test_columner.schema").toURI())

    var cr = new CSVColumnReader
    var cols = cr.readColumn(new File("src/test/resource/test_columner.csv").toURI(), schema)

    assertEquals(5, cols.size)

    cols.foreach(col => {
      assertEquals(1, Source.fromFile(col.colFile).getLines().size)
    })

    schema.hasHeader = false
    cols = cr.readColumn(new File("src/test/resource/test_columner.csv").toURI(), schema)

    assertEquals(5, cols.size)

    // Header will be treated as malformated
    cols.foreach(col => {
      assertEquals(1, Source.fromFile(col.colFile).getLines().size)
    })
  }

  @Test
  def testParseColumnReader2: Unit = {
    var schema = Schema.fromParquetFile(new File("src/test/resource/test_json_parser.schema").toURI())

    var cr = new JsonColumnReader
    var cols = cr.readColumn(new File("src/test/resource/test_json_parser.json").toURI(), schema)

    assertEquals(3, cols.size)

    cols.foreach(col => {
      assertEquals(5, Source.fromFile(col.colFile).getLines().size)
    })

  }
}