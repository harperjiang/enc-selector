package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex

object Arena extends App {

  val a = Nd4j.create(Array(Array(1d, 3, 9), Array(2d, 6, 1)))
  val b = Nd4j.create(Array(Array(2d, 2, 1), Array(3d, 6, 0)))

  val aext = a.get(NDArrayIndex.all(), NDArrayIndex.newAxis(), NDArrayIndex.all())
  val bext = b.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.newAxis())

//  println(aext.mmul(bext).shape().mkString(", "))

  val c = Nd4j.create(Array(2d, 3, 9)).reshape(3, 1).broadcast(3, 5)

  println(c)
}