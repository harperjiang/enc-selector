package edu.uchicago.cs.encsel.colread.csv

import java.io.File

import org.junit.Assert.assertEquals
import org.junit.Test

import edu.uchicago.cs.encsel.colread.Schema
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.encsel.colread.json.JsonColumnReader

class CSVColumnReaderTest {

  @Test
  def testReadColumn(): Unit = {
    
    
    var sourceFile = new File("resource/test_json_parser.csv").toURI()
    var ccr = new JsonColumnReader()
    var schema = Schema.fromParquetFile(new File("resource/test_json_parser.schema").toURI())
    var cols = ccr.readColumn(sourceFile, schema)

    assertEquals(3, cols.size)
    var arrays = cols.toArray
    
    assertEquals(0, arrays(0).colIndex)
    assertEquals("id", arrays(0).colName)
    assertEquals(DataType.INTEGER, arrays(0).dataType)
    assertEquals(sourceFile, arrays(0).origin)
    
    assertEquals(1, arrays(1).colIndex)
    assertEquals("name", arrays(1).colName)
    assertEquals(DataType.STRING, arrays(1).dataType)
    assertEquals(sourceFile, arrays(1).origin)
    
    assertEquals(2, arrays(2).colIndex)
    assertEquals("gender", arrays(2).colName)
    assertEquals(DataType.STRING, arrays(2).dataType)
    assertEquals(sourceFile, arrays(2).origin)
  }
}