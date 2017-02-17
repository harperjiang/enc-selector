package edu.uchicago.cs.encsel.app

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.BeforeClass
import edu.uchicago.cs.encsel.dataset.persist.DummyPersistence

class DataCollectorForTest extends DataCollector {

  def isDoneForTest(source: URI) = {
    isDone(source)
  }
  def markDoneForTest(source: URI) = {
    markDone(source)
  }
  override def collect(source: URI) = {
    this.synchronized {
      scanned += source
    }
  }

  var scanned = new ArrayBuffer[URI]();
}

object DataCollectorTest {
  @BeforeClass
  def deleteFile(): Unit = {
    Files.deleteIfExists(Paths.get(new File("src/test/resource/content.csv.done").toURI))
  }
}

class DataCollectorTest {

  @Test
  def testDone(): Unit = {
    var srcpath = new File("src/test/resource/test_columner.csv").toPath()
    var srcdone = new File("src/test/resource/test_columner.csv.done").toPath()
    var srcuri = srcpath.toUri()
    Files.deleteIfExists(srcdone)

    var dc = new DataCollectorForTest()
    assertFalse(dc.isDoneForTest(srcuri))
    dc.markDoneForTest(srcuri)
    assertTrue(dc.isDoneForTest(srcuri))
  }

  @Test
  def testCollect(): Unit = {
    var dc = new DataCollector
    var dp = new DummyPersistence
    dc.persistence = dp

    //    Files.deleteIfExists(Paths.get(new File("src/test/resource/content.csv.done").toURI))

    dc.collect(new File("src/test/resource/content.csv").toURI())
    //    assertTrue(Files.exists(Paths.get(new File("src/test/resource/content.csv.done").toURI)))

    var columns = dp.load()
    assertEquals(5, columns.size)
    columns.foreach { col =>
      {
        assertEquals(7, Source.fromFile(col.colFile).getLines().size)
      }
    }
  }

  @Test
  def testScan(): Unit = {
    var dc = new DataCollectorForTest

    dc.scan(new File("src/test/resource/scan_folder").toURI())

    assertEquals(5, dc.scanned.size)
    var fileNames = dc.scanned.map { Paths.get(_).getFileName.toString }

    assertTrue(fileNames.contains("c.json"))
    assertTrue(fileNames.contains("a.csv"))
    assertTrue(fileNames.contains("b.csv"))
    assertTrue(fileNames.contains("w.tsv"))
    assertTrue(fileNames.contains("n.txt"))
  }
}