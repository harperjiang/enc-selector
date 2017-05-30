package edu.uchicago.cs.ndnn.rnn

import edu.uchicago.cs.ndnn.Index
import org.junit.Test
import org.junit.Assert._
import org.nd4j.linalg.indexing.NDArrayIndex

class LSTMDatasetTest {

  @Test
  def testLoad: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val ds = new LSTMDataset(file)

    assertEquals(8, ds.dataSize)
    assertEquals(20, ds.dictSize)

    val batches = ds.batches(3).toArray
    assertEquals(5, batches.length)

    assertEquals(8, batches.map(_.size).sum)
  }
}