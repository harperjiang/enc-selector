package edu.uchicago.cs.encsel.ndnn.rnn

import edu.uchicago.cs.encsel.ndnn.SoftMaxLogLoss
import org.nd4j.linalg.factory.Nd4j
import org.junit.Test
import org.junit.Assert._
import org.nd4j.linalg.indexing.NDArrayIndex

class LSTMLossTest {

  @Test
  def testLoss: Unit = {
    val sloss = new LSTMLoss()

    val prob = Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
      Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
      Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d))).reshape(1, 3, 5)

    val label = Array(Array(3, 2, 4))

    val loss = sloss.loss(prob, label, true)
    assertEquals(Array(0.25d, 0.1d, 0.4d).map(-Math.log(_)).sum / 3, loss, 0.001)
    assertEquals(1, sloss.accuracy)
  }

  @Test
  def testMDLoss: Unit = {
    val sloss = new LSTMLoss()

    val prob = Nd4j.createUninitialized(Array(2, 3, 5))

    prob.put(Array(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.all()),
      Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
        Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
        Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d))))
    prob.put(Array(NDArrayIndex.point(1), NDArrayIndex.all(), NDArrayIndex.all()),
      Nd4j.create(Array(Array(0.1d, 0.1d, 0.25d, 0.25d, 0.1d),
        Array(0.2d, 0.3d, 0.1d, 0.20d, 0.20d),
        Array(0.3d, 0.1d, 0.15d, 0.15d, 0.3d))))

    val label = Array(Array(3, 2, 4), Array(1, 1, 0))

    val loss = sloss.loss(prob, label, true)
    val expected =
      assertEquals(Array(0.25d, 0.1d, 0.4d, 0.1, 0.3, 0.3).map(-Math.log(_)).sum / 6, loss, 0.001)
    assertEquals(3, sloss.accuracy)
  }

  @Test
  def testGrad: Unit = {
    val sloss = new LSTMLoss()
    val prob = Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
      Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
      Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d),
      Array(0, 0, 0, 0, 1d))).reshape(1, 4, 5)
    val label = Array(Array(3, 2, 4, 0))
    val loss = sloss.loss(prob, label, false)

    val grad = sloss.gradient

    assertArrayEquals(Array(1, 4, 5), grad.shape())
    for (i <- 0 to 3; j <- 0 to 4) {
      (i, j) match {
        case (0, 3) => assertEquals(-1 / (4 * 0.25), grad.getDouble(0, i, j), 0.01)
        case (1, 2) => assertEquals(-1 / (4 * 0.1), grad.getDouble(0, i, j), 0.01)
        case (2, 4) => assertEquals(-1 / (4 * 0.4), grad.getDouble(0, i, j), 0.01)
        case (3, 0) => assertEquals(-1 / (4 * SoftMaxLogLoss.clip), grad.getDouble(0, i, j), 1E11)
        case _ => assertEquals(0, grad.getDouble(0, i, j), 0.01)
      }
    }
    assertEquals(1, sloss.accuracy)
  }

  @Test
  def testMDGrad: Unit = {
    val sloss = new LSTMLoss()
    val prob = Nd4j.createUninitialized(Array(2, 3, 5))

    prob.put(Array(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.all()),
      Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
        Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
        Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d))))
    prob.put(Array(NDArrayIndex.point(1), NDArrayIndex.all(), NDArrayIndex.all()),
      Nd4j.create(Array(Array(0.1d, 0.1d, 0.25d, 0.25d, 0.1d),
        Array(0.2d, 0.3d, 0.1d, 0.20d, 0.20d),
        Array(0.3d, 0.1d, 0.15d, 0.15d, 0.3d))))
    val label = Array(Array(3, 2, 4), Array(1, 1, 0))

    val loss = sloss.loss(prob, label, false)

    val grad = sloss.gradient

    assertArrayEquals(Array(2, 3, 5), grad.shape())
    for (i <- 0 to 1; j <- 0 to 2; k <- 0 to 4) {
      (i, j, k) match {
        case (0, 0, 3) => assertEquals(-1 / (6 * 0.25), grad.getDouble(i, j, k), 0.01)
        case (0, 1, 2) => assertEquals(-1 / (6 * 0.1), grad.getDouble(i, j, k), 0.01)
        case (0, 2, 4) => assertEquals(-1 / (6 * 0.4), grad.getDouble(i, j, k), 0.01)
        case (1, 0, 1) => assertEquals(-1 / (6 * 0.1), grad.getDouble(i, j, k), 0.01)
        case (1, 1, 1) => assertEquals(-1 / (6 * 0.3), grad.getDouble(i, j, k), 0.01)
        case (1, 2, 0) => assertEquals(-1 / (6 * 0.3), grad.getDouble(i, j, k), 0.01)
        case _ => assertEquals(0, grad.getDouble(i, j, k), 0.01)
      }
    }
  }

  @Test
  def testAcc: Unit = {
    val sloss = new LSTMLoss()
    val prob = Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
      Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
      Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d))).reshape(1, 3, 5)
    val label = Array(Array(2, 1, 1))
    val loss = sloss.loss(prob, label, true)

    assertEquals(2, sloss.accuracy)
  }

}