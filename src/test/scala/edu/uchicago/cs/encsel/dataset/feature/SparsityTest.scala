package edu.uchicago.cs.encsel.dataset.feature

import org.junit.Test
import org.junit.Assert._
import java.io.File
import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.model.DataType

class SparsityTest {

  @Test
  def testRun(): Unit = {
    var col = new Column(new File("src/test/resource/test_columner.csv").toURI(), 0, "id", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_sparsity.data").toURI()

    var features = Sparsity.extract(col)
    assertEquals(3, features.size)

    var farray = features.toArray

    assertEquals("Sparsity", farray(0).featureType)
    assertEquals("count", farray(0).name)
    assertEquals(14, farray(0).value, 0.01)

    assertEquals("Sparsity", farray(1).featureType)
    assertEquals("empty_count", farray(1).name)
    assertEquals(4, farray(1).value, 0.01)
    
    assertEquals("Sparsity", farray(2).featureType)
    assertEquals("valid_ratio", farray(2).name)
    assertEquals(0.7142, farray(2).value, 0.01)

  }
}