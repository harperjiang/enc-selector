package edu.uchicago.cs.encsel.classify

import edu.uchicago.cs.encsel.model.DataType
import org.junit.Assert._
import org.junit.Test

/**
  * Created by harper on 4/20/17.
  */

class EncselDatasetForTest(value: Array[Array[Double]], label: Array[Double])
  extends EncselDataset(DataType.INTEGER) {
  override def load(): (Array[Array[Double]], Array[Double]) = {
    (value, label)
  }
}

class EncselDatasetTest {

  @Test
  def testSplit: Unit = {

    val data = (0 until 10000).map(i => Array.fill(100)(i.toDouble)).toArray
    val label = (0 until 10000).map(_.toDouble).toArray

    val dataset = new EncselDatasetForTest(data, label)

    val splitds = dataset.split(Seq(0.7, 0.15, 0.1))

    assertEquals(7000, splitds(0).dataSize)
    assertEquals(1500, splitds(1).dataSize)
    assertEquals(1500, splitds(2).dataSize)
  }
}
