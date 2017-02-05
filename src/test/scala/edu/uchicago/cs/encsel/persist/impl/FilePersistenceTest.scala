package edu.uchicago.cs.encsel.persist.impl

import java.io.File
import java.nio.file.Files

import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import scala.collection.mutable.ArrayBuffer
import edu.uchicago.cs.encsel.column.Column
import edu.uchicago.cs.encsel.model.DataType

class FilePersistenceTest {

  @Before
  def removeDataFile = {
    var path = new File("storage.dat").toPath()
    if (Files.exists(path))
      Files.delete(path)
  }

  @Test
  def testSave(): Unit = {
    var fp = new FilePersistence

    var dl = new ArrayBuffer[Column]()
    dl += new Column(null, 0, "", DataType.STRING)
    dl += new Column(null, 1, "", DataType.STRING)

    fp.save(dl)
  }

  @Test
  def testLoad(): Unit = {
    var fp = new FilePersistence

    fp.load()
  }

  @Test
  def testClean(): Unit = {
    var fp = new FilePersistence

    var dl = new ArrayBuffer[Column]()
    dl += new Column(null, 0, "", DataType.STRING)
    dl += new Column(null, 1, "", DataType.STRING)

    fp.save(dl)

    var dl2 = fp.load()
    assertEquals(2, dl2.size)

    fp.clean()

    dl2 = fp.load()
    assertEquals(0, dl2.size)
  }
}