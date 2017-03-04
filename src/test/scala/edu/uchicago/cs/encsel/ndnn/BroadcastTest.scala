package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.factory.Nd4j
import org.junit.Test
import org.junit.Assert._
import org.nd4j.linalg.indexing.NDArrayIndex

class BroadcastTest {

  @Test
  def testDiff: Unit = {
    val a = Nd4j.createUninitialized(Array(4, 2, 3, 5, 9))
    val b = Nd4j.createUninitialized(Array(4, 5, 8))

    val diff = Broadcast.diff(a.shape, b.shape)

    assertArrayEquals(Array(2), diff._1)
    assertArrayEquals(Array(0, 1, 4), diff._2)
  }
}