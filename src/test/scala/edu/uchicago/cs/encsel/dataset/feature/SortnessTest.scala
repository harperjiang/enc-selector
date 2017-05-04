package edu.uchicago.cs.encsel.dataset.feature

import java.io.File

import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.model.DataType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
  * Created by harper on 5/3/17.
  */
class SortnessTest {

  @Test
  def testExtract: Unit = {
    val col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_int.data").toURI
    val features = new Sortness(2).extract(col).toArray

    assertEquals(2, features.length)

    assertEquals("Sortness", features(0).featureType)
    assertEquals("2_totalpair", features(0).name)
    assertEquals(36, features(0).value, 0.001)

    assertEquals("Sortness", features(1).featureType)
    assertEquals("2_ivpair", features(1).name)
    assertEquals(0.7222, features(1).value, 0.001)
  }

  @Test
  def testExtractEmpty: Unit = {
    val col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_int.data").toURI
    val features = new Sortness(2).extract(col, FeatureExtractor.iidSamplingFilter(0.000001), "abc_").toArray

    assertEquals(2, features.length)

    assertEquals("abc_Sortness", features(0).featureType)
    assertEquals("2_totalpair", features(0).name)
    assertEquals(0, features(0).value, 0.001)

    assertEquals("abc_Sortness", features(1).featureType)
    assertEquals("2_ivpair", features(1).name)
    assertEquals(0, features(1).value, 0.001)
  }
}
