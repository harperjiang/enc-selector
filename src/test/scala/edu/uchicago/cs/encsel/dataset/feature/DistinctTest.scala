package edu.uchicago.cs.encsel.dataset.feature

import java.io.File

import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.model.DataType
import org.junit.Assert._
import org.junit.Test

/**
  * Created by harper on 4/14/17.
  */
class DistinctTest {

  @Test
  def testExtract: Unit = {
    val col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_str2.data").toURI
    //col.colFile = new File("/home/harper/Downloads/QUEUED_TIMESTAMP_06669881738871581783.tmp").toURI
    val features = Distinct.extract(col).toArray

    assertEquals(2, features.length)
    assertEquals("Distinct", features(0).featureType)
    assertEquals("count", features(0).name)
    assertEquals(7, features(0).value, 0.001)

    assertEquals("Distinct", features(1).featureType)
    assertEquals("ratio", features(1).name)
    assertEquals(0.7, features(1).value, 0.001)
  }

  @Test
  def testExtractWithFilter: Unit = {
    val col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_str2.data").toURI
    val features = Distinct.extract(col, FeatureExtractor.firstNFilter(3), "abc_").toArray

    assertEquals(2, features.length)
    assertEquals("abc_Distinct", features(0).featureType)
    assertEquals("count", features(0).name)
    assertEquals(3, features(0).value, 0.001)

    assertEquals("abc_Distinct", features(1).featureType)
    assertEquals("ratio", features(1).name)
    assertEquals(1, features(1).value, 0.001)
  }
}
