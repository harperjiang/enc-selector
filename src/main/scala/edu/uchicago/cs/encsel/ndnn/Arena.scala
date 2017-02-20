package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex

object Arena extends App {

  val a = Nd4j.create(Array(Array(1d, 3, 9), Array(2d, 6, 1)))

//  println(Nd4j.getExecutioner.execAndReturn(new org.nd4j.linalg.api.ops.impl.transforms.SoftMax(a)))
  println(Nd4j.getExecutioner.execAndReturn(new org.nd4j.linalg.api.ops.impl.transforms.SoftMax(a, null, a.dup(), a.lengthLong())))
}