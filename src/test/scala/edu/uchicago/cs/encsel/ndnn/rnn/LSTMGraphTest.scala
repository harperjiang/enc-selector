package edu.uchicago.cs.encsel.ndnn.rnn

import org.junit.Test
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.util.NDArrayUtil

class LSTMGraphTest {
  @Test
  def testGraphRun: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val batchsize = 50
    val ds = new LSTMDataset(file)
    ds.batchSize(batchsize)
    val batch = ds.batches.next().asInstanceOf[LSTMBatch]
    val hiddendim = 600
    val graph = new LSTMGraph(ds.numChars, hiddendim, batch.length, 0)
    // Set X input
    graph.xs.indices.foreach { i =>
      {
        val data = batch.data.get(NDArrayIndex.point(i), NDArrayIndex.all(), NDArrayIndex.all())
        graph.xs(i).setValue(data)
      }
    }
    // Set H0 and C0
    val emptyInit = Nd4j.zeros(batch.size, hiddendim)
    graph.h0.setValue(emptyInit)
    graph.c0.setValue(emptyInit)
    graph.expect(batch.groundTruth)

    val graph2 = new LSTMGraph(ds.numChars, hiddendim, 50, 1)
    val emptyInit2 = Nd4j.zeros(1, hiddendim)
    graph2.h0.setValue(emptyInit2)
    graph2.c0.setValue(emptyInit2)
    graph2.xs(0).setValue(Nd4j.create(Array(0d), Array(1, 1)))
    val predictLength = 50
    val alla = (0 until predictLength).map(i => 0d).toArray
    graph2.expect(Nd4j.create(alla, Array(predictLength, 1)))

    for (i <- 0 to 100) {
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
  }

  @Test
  def testGraphPredict: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val batchsize = 50
    val ds = new LSTMDataset(file)
    ds.batchSize(batchsize)
    val batch = ds.batches.next().asInstanceOf[LSTMBatch]
    val hiddendim = 500
    val graph = new LSTMGraph(ds.numChars, hiddendim, 10, 1)
    // Set X input
    val a = ds.translate("a")(0)
    graph.xs(0).setValue(Nd4j.create(Array(a), Array(1, 1)))
    // Set H0 and C0
    val emptyInit = Nd4j.zeros(1, hiddendim)
    graph.h0.setValue(emptyInit)
    graph.c0.setValue(emptyInit)
    val alla = (0 until 10).map(i => a).toArray
    graph.expect(Nd4j.create(alla, Array(10, 1)))

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