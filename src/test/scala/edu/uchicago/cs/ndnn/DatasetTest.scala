package edu.uchicago.cs.ndnn

import java.util.ArrayList
import java.util.Collections

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.mutable.HashSet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.util.Arrays

class DummyDataset extends DefaultDataset {
  def permuteIdx4Test = permuteIdx

  def load(): (Array[Array[Double]], Array[Double]) = {
    val data = new Array[Array[Double]](100)
    val gt = new Array[Double](100)

    for (i <- 0 until 100) {
      data(i) = (0 to 11).map(_ => i.toDouble).toArray
      gt(i) = i
    }
    (data, gt)
  }
}

class DatasetTest {

  @Test
  def testBatches: Unit = {
    val ds = new DummyDataset

    var array = ds.batches(27).toArray
    assertEquals(4, array.length)

    assertEquals(27, array(0).size)
    assertEquals(27, array(1).size)
    assertEquals(27, array(2).size)
    assertEquals(19, array(3).size)

    val distinct = new HashSet[Double]()
    array.foreach { batch => {
      (0 until batch.size).foreach {
        i => {
          val fromdata = batch.data.getDouble(i, 0)
          distinct += fromdata
          for (k <- 0 to 11) {
            assertEquals(batch.groundTruth.getDouble(i), batch.data.getDouble(i, k), 0.01)
          }
        }
      }
    }
    }
    assertEquals(100, distinct.size)
    assertEquals(0, distinct.min, 0.01)
    assertEquals(99, distinct.max, 0.01)

    distinct.clear()

    array = ds.batches(31).toArray
    assertEquals(4, array.length)

    assertEquals(31, array(0).size)
    assertEquals(31, array(1).size)
    assertEquals(31, array(2).size)
    assertEquals(7, array(3).size)

    array.foreach { batch => {
      (0 until batch.size).foreach {
        i => {
          val fromdata = batch.data.getDouble(i, 0)
          distinct += fromdata
          assertEquals(fromdata, batch.groundTruth.getDouble(i), 0.01)
          for (j <- 0 to 11) {
            assertEquals(fromdata, batch.data.getDouble(i, j), 0.01)
          }

        }
      }
    }
    }
    assertEquals(100, distinct.size)
    assertEquals(0, distinct.min, 0.01)
    assertEquals(99, distinct.max, 0.01)
  }
}