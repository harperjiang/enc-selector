package edu.uchicago.cs.encsel.ndnn.rnn

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.api.ndarray.INDArray
import edu.uchicago.cs.encsel.ndnn.SoftMaxLogLoss
import org.nd4j.linalg.ops.transforms.Transforms
import edu.uchicago.cs.encsel.ndnn.LossFunction
import edu.uchicago.cs.encsel.ndnn.Index

class LSTMLoss extends LossFunction[Array[Array[Int]]] {

  def forClassification = true
  /**
   * @param	actual		Probability of each label. This is an <code>INDArray</code>
   * 									of shape [L,B,N], where L is the chars in a sentence, B is the
   * 									batch size, N is the dimension
   * @param	expected	The ground truth label. This is an <code>Array[Array[Int]]</code> of
   * 									size L, B
   * @return 	mean of log loss of ground truth label in actual probability
   *
   */
  def loss(actual: INDArray, expected: Array[Array[Int]], fortest: Boolean): Double = {
    if (expected == null || expected.isEmpty) {
      throw new IllegalArgumentException("Expected value not set")
    }
    val shape = actual.shape()
    val l = shape(0)
    val b = shape(1)
    val n = shape.last

    val mask = Nd4j.zeros(l, b, n)
    val gt = Nd4j.zeros(l, b)
    expected.zipWithIndex.foreach {
      entry =>
        {
          val i = entry._2
          entry._1.zipWithIndex.foreach { e2 =>
            {
              val j = e2._2
              val k = e2._1
              mask.putScalar(Array(i, j, k), 1)
              gt.putScalar(Array(i, j), k)
            }
          }
        }
    }

    val clipped = Transforms.max(actual, SoftMaxLogLoss.clip, true)

    // Accuracy for classification
    val predict = Nd4j.argMax(actual, 2)
    val eq = predict.eps(gt)
    acc = eq.sumNumber().intValue()

    if (!fortest) {
      // -log(x) gradient
      val allone = Nd4j.onesLike(clipped).div(b * l)
      this.grad = allone.divi(clipped).negi().mul(mask)
    }

    Transforms.log(clipped, false).negi().muli(mask).sum(2).meanNumber().doubleValue()
  }
}