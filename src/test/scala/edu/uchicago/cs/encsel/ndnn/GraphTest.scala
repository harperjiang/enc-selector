package edu.uchicago.cs.encsel.ndnn

import org.junit.Assert.assertEquals
import org.junit.Test
import org.nd4j.linalg.factory.Nd4j

class GraphTest {

  @Test
  def testTrain: Unit = {
    var graph = new Graph(Xavier, new SGD(0.02), new SquareLoss())

    // Setup
    var x = graph.input("x")
    var w = graph.param("W", Array(3, 2))
    var b = graph.param("b", Array(1, 2))(Zero)

    var wx = new DotMul(x, w)
    var add = new Add(wx, b)
    graph.setOutput(add)

    x.setValue(Nd4j.create(Array(Array(3d, 2d, 1d), Array(5d, 7d, 6d), Array(8d, 2d, 9d), Array(6d, 4d, 1d))))
    graph.expect(Nd4j.create(Array(Array(25d, 31d), Array(79d, 112d), Array(43d, 110d), Array(49d, 54d))))
    var loss = Double.MaxValue
    for (i <- 0 to 500) {
      loss = graph.train
    }

    assertEquals(0, loss, 0.1)
  }

  @Test
  def testTest: Unit = {

    var graph = new Graph(Xavier, new SGD(0.01), new SoftMaxLogLoss())
    var x = graph.input("x")
    var w = graph.param("W", Array(3, 6))

    var wx = new DotMul(x, w)
    var softmax = new SoftMax(wx)
    graph.setOutput(softmax)

    x.setValue(Nd4j.create(Array(Array(1d, 2d, 3d), Array(2d, 1d, 9d), Array(5d, 2d, 7d))))
    w.setValue(Nd4j.create(Array(Array(0.1, 0.5, 2.7, 3.1, 0.2, 0.7), Array(1.1, 2.2, 1.3, 4.2, 5.2, 1.9), Array(3.8, 4.7, 2.2, 0.6, 1.9, 1.3))))

    graph.expect(Nd4j.create(Array(2d, 1d, 4d)).reshape(3, -1))

    assertEquals(22.27 / 3, graph.test, 0.01)
  }
}