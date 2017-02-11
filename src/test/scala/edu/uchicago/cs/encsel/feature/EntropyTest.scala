package edu.uchicago.cs.encsel.feature

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.column.Column
import edu.uchicago.cs.encsel.model.DataType
import java.io.File

class EntropyTest {

  @Test
  def testEntropyCalc(): Unit = {
    var calc = new EntropyCalc()

    calc.add("3244439239249-529023-23-9420-254239-0234-013=-421099042-24-2-42-942-492")
    assertEquals(2.81 * Math.log(2), calc.done(), 0.01)

    calc.reset()

    calc.add("23230-24=-sdakg394-3240ikgfdospaijgpi924-p09ewfkasd")
    calc.add("ofj29i32093fjoiprjfsaed-=]ewrger=-t30q3tiq4ju223roi29ot")
    calc.add("asdoifoiwerhjpwe3ewq32-90i239uoifdnhbldfgksdapogisdap09")
    assertEquals(Math.log(2) * 4.54415, calc.done(), 0.001)
  }

  @Test
  def testRun: Unit = {
    var col = new Column(null, -1, "", DataType.INTEGER)
    col.colFile = new File("src/test/resource/test_col_str2.data").toURI()

    var features = Entropy.extract(col).toArray

    assertEquals(5, features.size)
    assertEquals("line_max", features(0).name)
    assertEquals(2.0556, features(0).value, 0.001)
    assertEquals("line_min", features(1).name)
    assertEquals(1.4854, features(1).value, 0.001)
    assertEquals("line_mean", features(2).name)
    assertEquals(1.8107, features(2).value, 0.001)
    assertEquals("line_var", features(3).name)
    assertEquals(0.0374, features(3).value, 0.001)
    assertEquals("total", features(4).name)
    assertEquals(2.0694, features(4).value, 0.001)
  }

}