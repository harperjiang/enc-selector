package edu.uchicago.cs.encsel.ndnn

import org.junit.Test
import org.junit.Assert._
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
}