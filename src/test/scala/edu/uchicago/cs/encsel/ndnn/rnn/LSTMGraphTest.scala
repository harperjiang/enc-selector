package edu.uchicago.cs.encsel.ndnn.rnn

import org.junit.Test
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex

class LSTMGraphTest {
  @Test
  def testGraphRun: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val batchsize = 50
    val ds = new LSTMDataset(file)
    ds.batchSize(batchsize)
    val batch = ds.batches.next()
    val data = batch.data
    val hiddendim = 200
    val graph = new LSTMGraph(ds.numChars, hiddendim, data.length)
    // Set X input
    graph.xs.zip(data).foreach { pair =>
      {
        pair._1.set(pair._2)
      }
    }
    // Set H0 and C0
    val emptyInit = Nd4j.zeros(batch.size, hiddendim)
    graph.h0.set(emptyInit)
    graph.c0.set(emptyInit)
    graph.expect(batch.groundTruth)

    val graph2 = new LSTMPredictGraph(ds.numChars, hiddendim, 50, 1)
    val emptyInit2 = Nd4j.zeros(1, hiddendim)
    graph2.h0.set(emptyInit2)
    graph2.c0.set(emptyInit2)
    graph2.xs(0).set(Array(0))
    val predictLength = 50
    val alla = (0 until predictLength).map(i => Array(0)).toArray
    graph2.expect(alla)

    graph.train
    graph2.load(graph.dump())
    graph2.test

    println(graph2.xs.map {
      i =>
        {
          val char = i.value.getDouble(0, 0)
          ds.translate(char)
        }
    }.mkString(""))
  }

  @Test
  def testGraphPredict: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val batchsize = 50
    val ds = new LSTMDataset(file)
    ds.batchSize(batchsize)
    val batch = ds.batches.next()
    val hiddendim = 500
    val graph = new LSTMPredictGraph(ds.numChars, hiddendim, 10, 1)
    // Set X input
    val a = ds.translate("a")(0)
    graph.xs(0).set(Array(a))
    // Set H0 and C0
    val emptyInit = Nd4j.zeros(1, hiddendim)
    graph.h0.set(emptyInit)
    graph.c0.set(emptyInit)
    val alla = (0 until 10).map(i => Array(a)).toArray
    graph.expect(alla)

    graph.test

    println(graph.xs.map {
      i =>
        {
          val char = i.value.getDouble(0, 0)
          ds.translate(char)
        }
    }.mkString(""))
  }
}