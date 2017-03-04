package edu.uchicago.cs.encsel.ndnn.example.lstm

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastAddOp
import org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution
import org.nd4j.linalg.factory.Nd4j

import scala.util.Random

object Xavier {
  def init(shape: Array[Int]): INDArray = {
    var n = shape.dropRight(1).product
    var sd = Math.sqrt(3d / n)
    new UniformDistribution(-sd,sd).sample(shape)
  }
}

object LSTM extends App {

  val hiddenDim = 200
  val numChar = 100
  val c2v = Xavier.init(Array(numChar, hiddenDim))

  val v2c = Xavier.init(Array(hiddenDim, numChar))
  val inputSize = 2 * hiddenDim

  val w1 = Xavier.init(Array(inputSize, hiddenDim))
  val b1 = Nd4j.zeros(1, hiddenDim)
  val w2 = Xavier.init(Array(inputSize, hiddenDim))
  val b2 = Nd4j.zeros(1, hiddenDim)
  val w3 = Xavier.init(Array(inputSize, hiddenDim))
  val b3 = Nd4j.zeros(1, hiddenDim)
  val w4 = Xavier.init(Array(inputSize, hiddenDim))
  val b4 = Nd4j.zeros(1, hiddenDim)

  // Random Batch
  val length = 500
  val batchSize = 50
  val batch = (0 until length).map(i=> {
    Seq.fill(batchSize)(Random.nextInt(numChar)).toArray
  })
  val h0 = Nd4j.zeros(batchSize, hiddenDim)
  val c0 = Nd4j.zeros(batchSize, hiddenDim)

  val start = System.currentTimeMillis()

  val fwdmap = Nd4j.zeros(batchSize, numChar)
  batch.foreach{
    item => {
      fwdmap.assign(0)
      item.zipWithIndex.foreach(p => fwdmap.putScalar(Array(p._2, p._1), 1))
      val embedded = fwdmap.mmul(c2v)
      //val embedded = c2v.get(new SpecifiedIndex(item:_*),NDArrayIndex.all())
      val concat = Nd4j.concat(1, embedded, h0)
      val fgate = sigmoid(add(concat.mmul(w1), b1))
      val igate = sigmoid(add(concat.mmul(w2), b2))
    }
  }

  println((System.currentTimeMillis() - start).toDouble/1000)

  def add(a:INDArray, b:INDArray) = {
    Nd4j.getExecutioner.execAndReturn(new BroadcastAddOp(a,b,a,1))
  }

  def sigmoid(input:INDArray) =
    Nd4j.getExecutioner().execAndReturn(
      new org.nd4j.linalg.api.ops.impl.transforms.Sigmoid(input))

}