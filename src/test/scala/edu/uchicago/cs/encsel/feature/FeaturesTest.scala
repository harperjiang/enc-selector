package edu.uchicago.cs.encsel.feature

import org.junit.Test
import org.junit.Assert._
import java.io.File

class FeaturesTest {

  @Test
  def testFeatures: Unit = {
    var features = Features.extract(new File("resource/test_colreader.csv").toURI())
    var fa = features.toArray

    assertEquals(1, fa.size)
    assertTrue(fa(0).isInstanceOf[FileSize])
    assertEquals("file_size", fa(0).name)
    assertEquals(23.0, fa(0).value, 0.001)

  }
}