package edu.uchicago.cs.encsel.feature

import org.junit.Test
import org.junit.Assert._
import java.io.File

class FileSizeTest {

  @Test
  def testExtract: Unit = {
    var feature = FileSize.extract(new File("resource/test_colreader.csv").toURI())
    assertEquals(1, feature.size)
    var fa = feature.toArray
    assertTrue(fa(0).isInstanceOf[FileSize])
    assertEquals("file_size", fa(0).name)
    assertEquals(23.0, fa(0).value, 0.001)
  }
}