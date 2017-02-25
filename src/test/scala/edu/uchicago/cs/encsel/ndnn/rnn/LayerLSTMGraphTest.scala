package edu.uchicago.cs.encsel.ndnn.rnn

import org.junit.Assert._
import org.junit.Test
import org.nd4j.linalg.factory.Nd4j

class LayerLSTMGraphTest {

  @Test
  def testBuildAndTrain: Unit = {
    val layer = 2
    val numchar = 3
    val hiddendim = 4
    val len = 4
    val batchSize = 5
    val graph = new LayerLSTMGraph(layer, numchar, hiddendim, len)

    assertEquals(3, graph.xs.size)
    assertEquals(2, graph.h0.size)
    assertEquals(2, graph.c0.size)
    assertEquals(3, graph.getOutputs.length)

    val xval = Array(
      Nd4j.create(Array(2d, 1, 0, 1, 1), Array(5, 1)),
      Nd4j.create(Array(1d, 2, 1, 2, 0), Array(5, 1)),
      Nd4j.create(Array(0d, 1, 1, 2, 1), Array(5, 1)))
    val hval = (0 to 1).map(i => Nd4j.zeros(batchSize, hiddendim))
    val cval = (0 to 1).map(i => Nd4j.zeros(batchSize, hiddendim))
    graph.xs.zip(xval).foreach { p => p._1.setValue(p._2) }
    graph.h0.zip(hval).foreach { x => x._1.setValue(x._2) }
    graph.c0.zip(cval).foreach { x => x._1.setValue(x._2) }
    graph.expect(Nd4j.create(Array(1d, 2d, 0d, 2d, 2d, 1d, 1, 0, 1, 2d, 1, 1, 1, 0, 2), Array(3, 5, 1)))

    graph.train
  }
}