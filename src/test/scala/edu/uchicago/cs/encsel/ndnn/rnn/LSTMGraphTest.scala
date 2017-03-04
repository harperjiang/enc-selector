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
        pair._1.setRaw(pair._2)
      }
    }
    // Set H0 and C0
    val emptyInit = Nd4j.zeros(batch.size, hiddendim)
    graph.h0.setValue(emptyInit)
    graph.c0.setValue(emptyInit)
    graph.expect(batch.groundTruth)

    val graph2 = new LSTMPredictGraph(ds.numChars, hiddendim, 50, 1)
    val emptyInit2 = Nd4j.zeros(1, hiddendim)
    graph2.h0.setValue(emptyInit2)
    graph2.c0.setValue(emptyInit2)
    graph2.xs(0).setValue(Nd4j.create(Array(0d), Array(1, 1)))
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
    graph.xs(0).setValue(Nd4j.create(Array(a), Array(1, 1)))
    // Set H0 and C0
    val emptyInit = Nd4j.zeros(1, hiddendim)
    graph.h0.setValue(emptyInit)
    graph.c0.setValue(emptyInit)
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