package edu.uchicago.cs.encsel.dataset.feature

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.model.DataType
import java.io.File

class LengthTest {
  @Test
  def testRun: Unit = {
    val col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_str2.data").toURI()

    val features = Length.extract(col).toArray

    assertEquals(4, features.size)
    assertEquals("max", features(0).name)
    assertEquals(32, features(0).value, 0.001)
    assertEquals("min", features(1).name)
    assertEquals(8, features(1).value, 0.001)
    assertEquals("mean", features(2).name)
    assertEquals(23.167, features(2).value, 0.001)
    assertEquals("variance", features(3).name)
    assertEquals(68.4723, features(3).value, 0.001)
  }
}