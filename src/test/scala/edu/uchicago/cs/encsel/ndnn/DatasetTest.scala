package edu.uchicago.cs.encsel.ndnn

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

class DummyDataset extends DefaultDataset(Array(4, 3), Array(2, 2)) {
  def permuteIdx4Test = permuteIdx

  def load(): (Int, Array[INDArray], Array[INDArray]) = {
    val data = new Array[INDArray](100)
    val gt = new Array[INDArray](100)

    for (i <- 0 until 100) {
      data(i) = Nd4j.createUninitialized(dataShape).assign(i)
      gt(i) = Nd4j.createUninitialized(gtShape).assign(i)
    }
    (100, data, gt)
  }
}

class DatasetTest {

  @Test
  def testNewEpoch: Unit = {
    val ds = new DummyDataset
    val permuteCopy = new Array[Int](100)
    System.arraycopy(ds.permuteIdx4Test.toArray, 0, permuteCopy, 0, 100)
    ds.newEpoch()
    val permuteCopy2 = new Array[Int](100)
    System.arraycopy(ds.permuteIdx4Test.toArray, 0, permuteCopy2, 0, 100)

    assertEquals(100, permuteCopy.toSet.size)
    assertEquals(0, permuteCopy.min)
    assertEquals(99, permuteCopy.max)
    assertEquals(100, permuteCopy2.toSet.size)
    assertEquals(0, permuteCopy2.min)
    assertEquals(99, permuteCopy2.max)

    assertFalse(permuteCopy.sameElements(permuteCopy2))
  }

  @Test
  def testBatches: Unit = {
    val ds = new DummyDataset
    ds.batchSize(27)
    assertEquals(27, ds.batchSize)

    var array = ds.batches.toArray
    assertEquals(4, array.length)

    assertEquals(27, array(0).size)
    assertEquals(27, array(1).size)
    assertEquals(27, array(2).size)
    assertEquals(19, array(3).size)

    val distinct = new HashSet[Double]()
    array.foreach { batch =>
      {
        (0 until batch.size).foreach {
          i =>
            {
              val fromdata = batch.data.getDouble(i, 0, 0)
              distinct += fromdata
              for (j <- 0 to 3; k <- 0 to 2) {
                assertEquals(fromdata, batch.data.getDouble(i, j, k), 0.01)
              }
              for (j <- 0 to 1; k <- 0 to 1) {
                assertEquals(fromdata, batch.groundTruth.getDouble(i, j, k), 0.01)
              }
            }
        }
      }
    }
    assertEquals(100, distinct.size)
    assertEquals(0, distinct.min, 0.01)
    assertEquals(99, distinct.max, 0.01)

    distinct.clear()
    ds.newEpoch()
    ds.batchSize(31)

    array = ds.batches.toArray
    assertEquals(4, array.length)

    assertEquals(31, array(0).size)
    assertEquals(31, array(1).size)
    assertEquals(31, array(2).size)
    assertEquals(7, array(3).size)

    array.foreach { batch =>
      {
        (0 until batch.size).foreach {
          i =>
            {
              val fromdata = batch.data.getDouble(i, 0, 0)
              distinct += fromdata
              for (j <- 0 to 3; k <- 0 to 2) {
                assertEquals(fromdata, batch.data.getDouble(i, j, k), 0.01)
              }
              for (j <- 0 to 1; k <- 0 to 1) {
                assertEquals(fromdata, batch.groundTruth.getDouble(i, j, k), 0.01)
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