package edu.uchicago.cs.encsel.datacol.persist

import java.io.File
import java.nio.file.Files

import org.junit.Before
import org.junit.Test
import edu.uchicago.cs.encsel.model.Data
import scala.collection.mutable.ArrayBuffer

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

    var dl = new ArrayBuffer[Data]()
    dl += new Data()
    dl += new Data()

    fp.save(dl)
  }

  @Test
  def testLoad(): Unit = {
    var fp = new FilePersistence

    fp.load()
  }
}