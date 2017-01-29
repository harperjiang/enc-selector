package edu.uchicago.cs.encsel.colread

import org.junit.Test
import org.junit.Assert._
import java.io.File
import edu.uchicago.cs.encsel.model.DataType

class CSVColumnReaderTest {

  @Test
  def testReadColumn(): Unit = {
    var sourceFile = new File("resource/test_colreader.csv").toURI()
    var ccr = new CSVColumnReader()
    var schema = Schema.fromParquetFile(new File("resource/test_colreader.schema").toURI())
    var cols = ccr.readColumn(sourceFile, schema)

    assertEquals(5, cols.size)
    var arrays = cols.toArray
    
    assertEquals(0, arrays(0).colIndex)
    assertEquals("id", arrays(0).colName)
    assertEquals(DataType.INTEGER, arrays(0).dataType)
    assertEquals(sourceFile, arrays(0).orgin)
    
    assertEquals(1, arrays(1).colIndex)
    assertEquals("c1", arrays(1).colName)
    assertEquals(DataType.BOOLEAN, arrays(1).dataType)
    assertEquals(sourceFile, arrays(1).orgin)
    
    assertEquals(2, arrays(2).colIndex)
    assertEquals("c2", arrays(2).colName)
    assertEquals(DataType.FLOAT, arrays(2).dataType)
    assertEquals(sourceFile, arrays(2).orgin)
    
    assertEquals(3, arrays(3).colIndex)
    assertEquals("c3", arrays(3).colName)
    assertEquals(DataType.STRING, arrays(3).dataType)
    assertEquals(sourceFile, arrays(3).orgin)
    
    assertEquals(4, arrays(4).colIndex)
    assertEquals("c4", arrays(4).colName)
    assertEquals(DataType.INTEGER, arrays(4).dataType)
    assertEquals(sourceFile, arrays(4).orgin)
  }
}