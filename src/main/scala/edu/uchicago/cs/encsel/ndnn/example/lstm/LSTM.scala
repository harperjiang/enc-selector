package edu.uchicago.cs.encsel.ndnn.example.lstm

import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph
import org.nd4j.linalg.factory.Nd4j

object LSTM extends App {

  val datafile = "/home/harper/dataset/lstm/ptb.train.txt"
  val lstmds = new LSTMDataset(datafile)

  val hiddenDim = 200

  lstmds.batchSize(50)
  val start = System.currentTimeMillis()
  lstmds.batches.foreach(batch => {
    val data = batch.data
    val l = data.length
    val b = batch.size
    val groundTruth = batch.groundTruth
    val trainGraph = new LSTMGraph(lstmds.numChars, hiddenDim, data.size)

    val zeroinit = Nd4j.zeros(b, hiddenDim)

    trainGraph.h0.value = zeroinit
    trainGraph.c0.value = zeroinit
    trainGraph.xs.zip(data).foreach(pair => pair._1.set(pair._2))

    trainGraph.expect(groundTruth)

    trainGraph.train
  })
  println((System.currentTimeMillis() - start) / 60000)
}