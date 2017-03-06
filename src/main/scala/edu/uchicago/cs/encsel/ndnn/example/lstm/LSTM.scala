package edu.uchicago.cs.encsel.ndnn.example.lstm

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastAddOp
import org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution
import org.nd4j.linalg.factory.Nd4j

import scala.util.Random
import edu.uchicago.cs.encsel.ndnn.Xavier
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset
import edu.uchicago.cs.encsel.ndnn.Trainer
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph
import edu.uchicago.cs.encsel.ndnn.Batch

class LSTMTrainer(ts: LSTMDataset, tsts: LSTMDataset, hiddenDim: Int) extends Trainer[Array[Array[Int]], Array[Array[Int]], LSTMDataset, LSTMGraph] {

  val graph = new LSTMGraph(ts.numChars, hiddenDim)

  def getTrainset: LSTMDataset = ts
  def getTestset: LSTMDataset = tsts
  def getTrainedGraph: LSTMGraph = graph

  def getGraph(batch: Batch[Array[Array[Int]], Array[Array[Int]]]): LSTMGraph = {
    graph.build(batch.data.length)

    // Setup x_i input
    batch.data.zip(graph.xs).foreach(pair => pair._2.set(pair._1))
    // Setup h_0 and c_0
    graph.h0.set(Nd4j.zeros(batch.size, hiddenDim))
    graph.c0.set(Nd4j.zeros(batch.size, hiddenDim))
    // Expect
    graph.expect(batch.groundTruth)
    graph
  }
  
  override protected def evaluate(testBatchSize: Int = 100): Unit = {
    val testset = getTestset
    var numBatch = 0
    var totalLoss = 0d
    testset.batches(testBatchSize).foreach {
      batch =>
        {
          val graph = getGraph(batch)
          val (loss, acc) = graph.test
          totalLoss += loss
          numBatch += 1
        }
    }
    // TODO Log info
  }
}

object LSTM extends App {

  val hiddenDim = 200
  val batchSize = 50

  val trainds = new LSTMDataset("/home/harper/dataset/lstm/ptb.train.txt")
  val testds = new LSTMDataset("/home/harper/dataset/lstm/ptb.test.txt")(trainds)

  val trainer = new LSTMTrainer(trainds, testds, hiddenDim)

  trainer.train(30, true, 50)
}