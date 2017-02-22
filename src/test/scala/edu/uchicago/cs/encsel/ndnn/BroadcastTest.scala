package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.factory.Nd4j
import org.junit.Test
import org.junit.Assert._
import org.nd4j.linalg.indexing.NDArrayIndex

class BroadcastTest {
  @Test
  def testBroadcast: Unit = {
    val a = Nd4j.createUninitialized(Array(3, 2, 5))

    a.put(Array(NDArrayIndex.point(0)), Nd4j.createUninitialized(2, 5).assign(4))
    a.put(Array(NDArrayIndex.point(1)), Nd4j.createUninitialized(2, 5).assign(3))
    a.put(Array(NDArrayIndex.point(2)), Nd4j.createUninitialized(2, 5).assign(9))

    val broadcasted = Broadcast.broadcast(a, Array(4, 3, 2, 5))

    assertArrayEquals(Array(4, 3, 2, 5), broadcasted.shape())

    for (i <- 0 to 3; j <- 0 to 2; k <- 0 to 1; l <- 0 to 4) {
      j match {
        case 0 => assertEquals(4, broadcasted.getDouble(i, j, k, l), 0.001)
        case 1 => assertEquals(3, broadcasted.getDouble(i, j, k, l), 0.001)
        case 2 => assertEquals(9, broadcasted.getDouble(i, j, k, l), 0.001)
      }
    }

    val a2 = Nd4j.create(Array(3d, 9, 8, 0))
    val a21 = Broadcast.broadcast(a2, Array(5, 4))
    val a22 = Broadcast.broadcast(a2.transpose(), Array(4, 5))

    for (i <- 0 to 3; j <- 0 to 4) {
      assertEquals(a2.getDouble(i), a21.getDouble(j, i), 0.01)
      assertEquals(a2.getDouble(i), a22.getDouble(i, j), 0.01)
    }
  }

  @Test
  def testDiff: Unit = {
    val a = Nd4j.createUninitialized(Array(4, 2, 3, 5, 9))
    val b = Nd4j.createUninitialized(Array(4, 5, 8))

    val diff = Broadcast.diff(a.shape, b.shape)

    assertArrayEquals(Array(2), diff._1)
    assertArrayEquals(Array(0, 1, 4), diff._2)
  }
}