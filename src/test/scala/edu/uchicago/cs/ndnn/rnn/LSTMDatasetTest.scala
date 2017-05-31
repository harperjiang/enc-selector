package edu.uchicago.cs.ndnn.rnn

import org.junit.Assert._
import org.junit.Test

class LSTMDatasetTest {

  @Test
  def testLoad: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val ds = new LSTMDataset(file, new WordTokenizer(), "<s>", "</s>")

    assertEquals(8, ds.dataSize)
    assertEquals(22, ds.dictSize)

    val batches = ds.batches(3).toArray
    assertEquals(5, batches.length)

    assertEquals(8, batches.map(_.size).sum)
  }
}