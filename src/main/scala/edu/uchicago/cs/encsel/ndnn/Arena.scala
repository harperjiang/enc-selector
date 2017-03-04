package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.indexing.SpecifiedIndex
import scala.collection.mutable.ArrayBuffer
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastAddOp

object Arena extends App {

  // Test ND4J performance of copying and assign

  val a = Nd4j.create((0 to 11).map(_.toDouble).toArray).reshape(2, 2, 3)
  val b = Nd4j.create(Array(10d, 20, 30, 40, 50, 60)).reshape(2, 3)

  val x = Nd4j.getExecutioner.execAndReturn(new BroadcastAddOp(a, b, a, 1, 2))
  println(x)
}