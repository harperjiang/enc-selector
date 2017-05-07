package edu.uchicago.cs.encsel.dataset.feature

import java.io.File
import java.util.Comparator

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
    assertEquals("totalpair_2", features(0).name)
    assertEquals(6, features(0).value, 0.001)

    assertEquals("Sortness", features(1).featureType)
    assertEquals("ivpair_2", features(1).name)
    assertEquals(0.6667, features(1).value, 0.001)

    val features2 = new Sortness(-1).extract(col).toArray

    assertEquals(2, features2.length)

    assertEquals("Sortness", features2(0).featureType)
    assertEquals("totalpair_-1", features2(0).name)
    assertEquals(78, features2(0).value, 0.001)

    assertEquals("Sortness", features2(1).featureType)
    assertEquals("ivpair_-1", features2(1).name)
    assertEquals(0.6923, features2(1).value, 0.001)
  }

  @Test
  def testExtractEmpty: Unit = {
    val col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_int.data").toURI
    val features = new Sortness(2).extract(col, Filter.iidSamplingFilter(0.000001), "abc_").toArray

    assertEquals(2, features.length)

    assertEquals("abc_Sortness", features(0).featureType)
    assertEquals("totalpair_2", features(0).name)
    assertEquals(0, features(0).value, 0.001)

    assertEquals("abc_Sortness", features(1).featureType)
    assertEquals("ivpair_2", features(1).name)
    assertEquals(0, features(1).value, 0.001)
  }

  @Test
  def testExtractEmptyColumn: Unit = {
    val col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_empty.dat").toURI
    val features = new Sortness(2).extract(col).toArray

    assertEquals(2, features.length)

    assertEquals("Sortness", features(0).featureType)
    assertEquals("totalpair_2", features(0).name)
    assertEquals(12, features(0).value, 0.001)

    assertEquals("Sortness", features(1).featureType)
    assertEquals("ivpair_2", features(1).name)
    assertEquals(0, features(1).value, 0.001)
  }

  @Test
  def testComputeInvertPair: Unit = {
    val input = Seq(1, 3, 0, 2, 5, 7, 8, 9, 6)

    val (inv, total) = Sortness.computeInvertPair(input.map(_.toString), DataType.INTEGER.comparator())

    assertEquals(6, inv)
    assertEquals(36, total)
  }
}
