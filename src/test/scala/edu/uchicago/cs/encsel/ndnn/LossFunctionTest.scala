package edu.uchicago.cs.encsel.ndnn

import org.junit.Assert._
import org.junit.Test
import org.nd4j.linalg.factory.Nd4j

class SoftMaxLogLossTest {

  @Test
  def testLoss: Unit = {
    val sloss = new SoftMaxLogLoss()

    val prob = Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
      Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
      Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d)))

    val label = Nd4j.create(Array(3d, 2d, 4d)).reshape(3, 1)

    val loss = sloss.loss(prob, label)
    assertEquals(Array(0.25d, 0.1d, 0.4d).map(-Math.log(_)).sum / 3, loss, 0.001)
  }

  @Test
  def testGrad: Unit = {
    val sloss = new SoftMaxLogLoss()
    val prob = Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
      Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
      Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d)))
    val label = Nd4j.create(Array(3d, 2d, 4d)).reshape(3, 1)
    val loss = sloss.loss(prob, label)

    val grad = sloss.gradient

    assertArrayEquals(Array(3, 5), grad.shape())
    for (i <- 0 to 2; j <- 0 to 4) {
      (i, j) match {
        case (0, 3) => assertEquals(-1 / (3 * 0.25), grad.getDouble(i, j), 0.01)
        case (1, 2) => assertEquals(-1 / (3 * 0.1), grad.getDouble(i, j), 0.01)
        case (2, 4) => assertEquals(-1 / (3 * 0.4), grad.getDouble(i, j), 0.01)
        case _ => assertEquals(0, grad.getDouble(i, j), 0.01)
      }
    }
  }

  @Test
  def testAcc: Unit = {
    val sloss = new SoftMaxLogLoss()
    val prob = Nd4j.create(Array(Array(0.1d, 0.2d, 0.35d, 0.25d, 0.1d),
      Array(0.2d, 0.3d, 0.1d, 0.15d, 0.25d),
      Array(0.3d, 0.1d, 0.15d, 0.05d, 0.4d)))
    val label = Nd4j.create(Array(2d, 1d, 1d)).reshape(3, 1)
    val loss = sloss.loss(prob, label, true)

    assertEquals(2, sloss.accuracy)
  }
}