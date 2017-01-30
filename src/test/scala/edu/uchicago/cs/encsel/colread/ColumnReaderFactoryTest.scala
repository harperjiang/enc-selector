package edu.uchicago.cs.encsel.colread

import edu.uchicago.cs.encsel.datacol.DataCollectorForTest
import edu.uchicago.cs.encsel.colread.csv.CSVColumnReader
import java.io.File
import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.colread.tsv.TSVColumnReader
import edu.uchicago.cs.encsel.colread.json.JsonColumnReader

class ColumnReaderFactoryTest {
  @Test
  def testGetColumnReader(): Unit = {
    var cr = ColumnReaderFactory.getColumnReader(new File("resource/test_colreader.csv").toURI())
    assertTrue(cr.isInstanceOf[CSVColumnReader])

    cr = ColumnReaderFactory.getColumnReader(new File("resource/test_colreader.tsv").toURI())
    assertTrue(cr.isInstanceOf[TSVColumnReader])
    
    cr = ColumnReaderFactory.getColumnReader(new File("resource/test_json_parser.json").toURI())
    assertTrue(cr.isInstanceOf[JsonColumnReader])
  }
}