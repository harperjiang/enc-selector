package edu.uchicago.cs.ndnn.rnn

import org.junit.Test
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex

class LSTMGraphTest {
  @Test
  def testGraphRun: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val batchsize = 50
    val ds = new LSTMDataset(file)
    val batch = ds.batches(batchsize).next()
    val data = batch.feature(0)
    val hiddendim = 200

    val graph = new LSTMGraph(ds.dictSize, hiddendim)
    graph.build(batch)
    graph.train
  }

}