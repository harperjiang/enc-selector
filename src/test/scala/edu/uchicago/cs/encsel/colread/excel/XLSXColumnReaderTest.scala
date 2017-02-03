package edu.uchicago.cs.encsel.colread.excel

import org.junit.Test
import org.junit.Assert._
import java.io.File
import edu.uchicago.cs.encsel.colread.Schema
import scala.io.Source

class XLSXColumnReaderTest {

  @Test
  def testRead(): Unit = {
    var cr = new XLSXColumnReader()
    var schema = Schema.fromParquetFile(new File("src/test/resource/test_col_reader_xlsx.schema").toURI())

    var columns = cr.readColumn(new File("src/test/resource/test_col_reader_xlsx.xlsx").toURI, schema)

    assertEquals(12, columns.size)

    columns.foreach(col => {
      assertEquals(11, Source.fromFile(col.colFile).getLines().size)
    })
  }
}