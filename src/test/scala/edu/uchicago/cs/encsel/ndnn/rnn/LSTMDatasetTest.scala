package edu.uchicago.cs.encsel.ndnn.rnn

import org.junit.Test

class LSTMDatasetTest {

  @Test
  def testLoad: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val ds = new LSTMDataset(file)
  }
}