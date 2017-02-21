package edu.uchicago.cs.encsel.ndnn

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.api.ops.impl.transforms.SoftMaxDerivative

class NodeTest {

  @Test
  def testBroadcast: Unit = {
    val a = Nd4j.createUninitialized(Array(3, 2, 5))

    a.put(Array(NDArrayIndex.point(0)), Nd4j.createUninitialized(2, 5).assign(4))
    a.put(Array(NDArrayIndex.point(1)), Nd4j.createUninitialized(2, 5).assign(3))
    a.put(Array(NDArrayIndex.point(2)), Nd4j.createUninitialized(2, 5).assign(9))

    val broadcasted = Node.broadcast(a, Array(4, 3, 2, 5))

    assertArrayEquals(Array(4, 3, 2, 5), broadcasted.shape())

    for (i <- 0 to 3; j <- 0 to 2; k <- 0 to 1; l <- 0 to 4) {
      j match {
        case 0 => assertEquals(4, broadcasted.getDouble(i, j, k, l), 0.001)
        case 1 => assertEquals(3, broadcasted.getDouble(i, j, k, l), 0.001)
        case 2 => assertEquals(9, broadcasted.getDouble(i, j, k, l), 0.001)
      }
    }

    val a2 = Nd4j.create(Array(3d, 9, 8, 0))
    val a21 = Node.broadcast(a2, Array(5, 4))
    val a22 = Node.broadcast(a2.transpose(), Array(4, 5))

    for (i <- 0 to 3; j <- 0 to 4) {
      assertEquals(a2.getDouble(i), a21.getDouble(j, i), 0.01)
      assertEquals(a2.getDouble(i), a22.getDouble(i, j), 0.01)
    }
  }

  @Test
  def testDiff: Unit = {
    val a = Nd4j.createUninitialized(Array(4, 2, 3, 5, 9))
    val b = Nd4j.createUninitialized(Array(4, 5, 8))

    val diff = Node.diff(a.shape, b.shape)

    assertArrayEquals(Array(2), diff._1)
    assertArrayEquals(Array(0, 1, 4), diff._2)
  }

  @Test
  def testForward: Unit = {

    val node1 = new Input()
    val node2 = new Input()
    val node3 = new DummyNode(node1, node2)
    val node4 = new DummyNode(node3)
    val node5 = new DummyNode(node3)
    val node6 = new DummyNode(node4)
    val node7 = new DummyNode(node2, node5, node6)

    node1.value = Nd4j.createUninitialized(Array(2, 3, 5)).assign(1)
    node2.value = Nd4j.createUninitialized(Array(4, 9, 7)).assign(2)
    node1.forward(node1)
    node2.forward(node2)

    assertEquals(node2.value, node7.value)
  }

  @Test
  def testBackward: Unit = {
    val node1 = new Input("node1")
    val node2 = new Input()
    val node3 = new DummyNode(node1, node2)
    val node4 = new DummyNode(node3)
    val node5 = new DummyNode(node3)
    val node6 = new DummyNode(node4)
    val node7 = new DummyNode(node2, node5, node6)

    node7.backward(node7, Nd4j.createUninitialized(Array(3, 2, 7)).assign(1))

    assertArrayEquals(Array(3, 2, 7), node1.grad.shape)
    assertArrayEquals(Array(3, 2, 7), node2.grad.shape)

    for (i <- 0 to 2; j <- 0 to 1; k <- 0 to 6) {
      assertEquals(2, node1.grad.getDouble(i, j, k), 0.001)
      assertEquals(3, node2.grad.getDouble(i, j, k), 0.001)
    }
  }

}

class SoftMaxTest {
  @Test
  def testCalculate: Unit = {
    val input = new Input()
    val softmax = new SoftMax(input)

    input.setValue(Nd4j.create(Array(Array(1d, 2d, 3d, 4d, 5d), Array(2d, 7d, 6d, 2d, 3d), Array(1d, 1d, 2d, 2d, 3d))))
    val inputbackup = input.value.dup()
    softmax.forward(input)

    val result = softmax.value
    assertArrayEquals(Array(3, 5), result.shape())
    val expected = Array(Array(0.01165623, 0.03168492, 0.08612854, 0.23412166, 0.63640865),
      Array(0.00481395, 0.71445362, 0.2628328, 0.00481395, 0.01308567),
      Array(0.06745081, 0.06745081, 0.1833503, 0.1833503, 0.49839779))
    for (i <- 0 until 3; j <- 0 until 5) {
      assertEquals(input.value.getDouble(i, j), inputbackup.getDouble(i, j), 0.01)
      assertEquals(expected(i)(j), result.getDouble(i, j), 0.01)
    }
  }

  @Test
  def testDerivative: Unit = {
    val input = new Input()
    val softmax = new SoftMax(input)

    input.setValue(Nd4j.create(Array(Array(1d, 2d, 3d, 4d, 5d), Array(2d, 7d, 6d, 2d, 3d), Array(1d, 1d, 2d, 2d, 3d))))

    softmax.forward(input)

    val grad = Nd4j.create(Array(Array(0, 0, 1d / 3, 0, 0), Array(0, 2d / 3, 0, 0, 0), Array(0, 0, 0, 0, 1.5 / 3)))

    softmax.backward(softmax, grad)
    val result = input.grad

    println(softmax.value)

    val expected = Array(Array(-0.00033464, -0.00090966, 0.02623681, -0.00672152, -0.01827098),
      Array(-0.0022929, 0.13600643, -0.1251879, -0.0022929, -0.00623274),
      Array(-0.01680867, -0.01680867, -0.04569069, -0.04569069, 0.12499872))
    for (i <- 0 until 3; j <- 0 until 5) {
      assertEquals(expected(i)(j), result.getDouble(i, j), 0.0001)
    }
  }
}

class SigmoidTest {

  @Test
  def testCompute: Unit = {
    val input = new Input()
    val sigmoid = new Sigmoid(input)
    input.setValue(Nd4j.create(Array(Array(0.4, 0.9, -1.1, 3.8), Array(2.2, 1.7, 0.5, -0.7))))

    input.forward(input)

    assertArrayEquals(Array(2, 4), sigmoid.value.shape())

    val expected = Nd4j.create(Array(Array(0.59868766, 0.7109495, 0.24973989, 0.97811873),
      Array(0.90024951, 0.84553473, 0.62245933, 0.33181223)))
    for (i <- 0 to 1; j <- 0 to 3) {
      assertEquals(expected.getDouble(i, j), sigmoid.value.getDouble(i, j), 0.001)
    }
  }

  @Test
  def testUpdateGrad: Unit = {
    val input = new Input()
    val sigmoid = new Sigmoid(input)
    sigmoid.value = Nd4j.create(Array(Array(0.59868766, 0.7109495, 0.24973989, 0.97811873),
      Array(0.90024951, 0.84553473, 0.62245933, 0.33181223)))
    val grad = Nd4j.create(Array(Array(1, 0, 2, 4d), Array(2, 1, 7, 5d)))
    sigmoid.backward(sigmoid, grad)
    
    val expected = grad.muli(sigmoid.value.mul(sigmoid.value.sub(1).negi()))
    println(input.grad)
    for (i <- 0 to 1; j <- 0 to 3) {
      assertEquals(expected.getDouble(i, j), input.grad.getDouble(i, j), 0.001)
    }
  }

}