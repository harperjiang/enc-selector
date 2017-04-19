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

    val batches = ds.batches(3).toArray
    assertEquals(3, batches.length)
    assertEquals(3, batches(0).size)
    assertEquals(3, batches(1).size)
    assertEquals(2, batches(2).size)
    assertEquals(24, ds.numChars)

  }
}