package edu.uchicago.cs.encsel.datacol

import java.io.File
import java.net.URI

import org.junit.Assert._
import org.junit.Test

import edu.uchicago.cs.encsel.colread.CSVColumnReader
import java.nio.file.Files

class DataCollectorForTest extends DataCollector {
  def getColumnReaderForTest(source: URI) = {
    getColumnReader(source)
  }
  def getSchemaForTest(source: URI) = {
    getSchema(source)
  }
  def isDoneForTest(source: URI) = {
    isDone(source)
  }
  def markDoneForTest(source: URI) = {
    markDone(source)
  }
}

class DataCollectorTest {

  @Test
  def testGetColumnReader(): Unit = {
    var dc = new DataCollectorForTest()
    var cr = dc.getColumnReaderForTest(new File("resource/test_colreader.csv").toURI())
    assertTrue(cr.isInstanceOf[CSVColumnReader])
  }

  @Test
  def testGetSchema(): Unit = {
    var dc = new DataCollectorForTest()
    var schema = dc.getSchemaForTest(new File("resource/test_colreader.csv").toURI())
    assertEquals(5, schema.columns.length)
  }

  @Test
  def testDone(): Unit = {
    var srcpath = new File("resource/test_colreader.csv").toPath()
    var srcdone = new File("resource/test_colreader.csv.done").toPath()
    var srcuri = srcpath.toUri()
    Files.deleteIfExists(srcdone)

    var dc = new DataCollectorForTest()
    assertFalse(dc.isDoneForTest(srcuri))
    dc.markDoneForTest(srcuri)
    assertTrue(dc.isDoneForTest(srcuri))
  }
}