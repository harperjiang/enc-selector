package edu.uchicago.cs.encsel.colread

import java.io.File

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.colread.csv.CSVColumnReader
import scala.io.Source

class ParserColumnReaderTest {

  @Test
  def testParseColumnReader: Unit = {
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
}