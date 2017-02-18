package edu.uchicago.cs.encsel.ndnn

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex

class NodeTest {

  @Test
  def testBroadcast: Unit = {
    var a = Nd4j.createUninitialized(Array(3, 2, 5))

    a.put(Array(NDArrayIndex.point(0)), Nd4j.createUninitialized(2, 5).assign(4))
    a.put(Array(NDArrayIndex.point(1)), Nd4j.createUninitialized(2, 5).assign(3))
    a.put(Array(NDArrayIndex.point(2)), Nd4j.createUninitialized(2, 5).assign(9))

    var broadcasted = Node.broadcast(a, Array(4, 3, 2, 5))

    assertArrayEquals(Array(4, 3, 2, 5), broadcasted.shape())

    for (i <- 0 to 3; j <- 0 to 2; k <- 0 to 1; l <- 0 to 4) {
      j match {
        case 0 => assertEquals(4, broadcasted.getDouble(i, j, k, l), 0.001)
        case 1 => assertEquals(3, broadcasted.getDouble(i, j, k, l), 0.001)
        case 2 => assertEquals(9, broadcasted.getDouble(i, j, k, l), 0.001)
      }

    }
  }

  @Test
  def testDiff: Unit = {
    var a = Nd4j.createUninitialized(Array(4, 2, 3, 5, 9))
    var b = Nd4j.createUninitialized(Array(4, 5, 8))

    var diff = Node.diff(a.shape, b.shape)

    assertArrayEquals(Array(2), diff._1)
    assertArrayEquals(Array(0, 1, 4), diff._2)
  }
}