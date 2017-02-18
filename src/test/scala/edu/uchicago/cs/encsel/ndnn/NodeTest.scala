package edu.uchicago.cs.encsel.ndnn

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex

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
  def testForward:Unit = {

    val node1 = new DummyNode()
    val node2 = new DummyNode()
    val node3 = new DummyNode(node1,node2)
    val node4 = new DummyNode(node3)
    val node5 = new DummyNode(node3)
    val node6 = new DummyNode(node4)
    val node7 = new DummyNode(node2,node5, node6)

    node1.value = Nd4j.createUninitialized(Array(2,3,5))
    node2.value = Nd4j.createUninitialized(Array(4,9,7))
    node1.forward(node1)
    node2.forward(node2)

    assertEquals(node2.value, node7.value)
  }

  @Test
  def testBackward:Unit = {
    val node1 = new DummyNode()
    val node2 = new DummyNode()
    val node3 = new DummyNode(node1,node2)
    val node4 = new DummyNode(node3)
    val node5 = new DummyNode(node3)
    val node6 = new DummyNode(node4)
    val node7 = new DummyNode(node2, node5, node6)

    node7.backward(node7, Nd4j.createUninitialized(Array(3,2,7)).assign(1))

    assertArrayEquals(Array(3,2,7), node1.grad.shape)
    assertArrayEquals(Array(3,2,7), node2.grad.shape)

    for(i<- 0 to 2; j<-0 to 1;k <- 0 to 6){
       assertEquals(2,node1.grad.getDouble(i,j,k),0.001)
      assertEquals(3,node2.grad.getDouble(i,j,k), 0.001)
      }
  }

}