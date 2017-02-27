package edu.uchicago.cs.encsel.ndnn.rnn

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.ndnn.Index
import org.nd4j.linalg.indexing.NDArrayIndex

class LSTMDatasetTest {

  @Test
  def testLoad: Unit = {
    val file = "src/test/resource/rnn/lstm_sample_ds"
    val ds = new LSTMDataset(file)

    assertEquals(8, ds.size)

    ds.batchSize(3)
    val batches = ds.batches.map(_.asInstanceOf[LSTMBatch]).toArray
    assertEquals(3, batches.length)
    assertEquals(3, batches(0).size)
    assertEquals(3, batches(1).size)
    assertEquals(2, batches(2).size)
    assertEquals(24, ds.numChars)
    assertEquals(24, batches(0).length)
    assertEquals(13, batches(1).length)
    assertEquals(10, batches(2).length)

    assertArrayEquals(Array(24, 3, 1), batches(0).data.shape)
    assertArrayEquals(Array(24, 3, 1), batches(0).groundTruth.shape)
    assertArrayEquals(Array(13, 3, 1), batches(1).data.shape)
    assertArrayEquals(Array(13, 3, 1), batches(1).groundTruth.shape)
    assertArrayEquals(Array(10, 2, 1), batches(2).data.shape)
    assertArrayEquals(Array(10, 2, 1), batches(2).groundTruth.shape)

    val firstwords = "{it is a good day to die"
    val translated = ds.translate(firstwords)

    val real = batches(0).data.get(NDArrayIndex.all(), NDArrayIndex.point(0), NDArrayIndex.point(0))

    translated.indices.foreach(i => assertEquals(translated(i), real.getDouble(i), 0.001))
  }
}