package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.indexing.SpecifiedIndex

object Arena extends App {

  val a = Nd4j.create(Array(Array(3, 6, 8, 7, 6d), Array(4d, 5, 2, 3, 1), Array(5, 2, 1, 9, 0d)))
  val b = a.get(new SpecifiedIndex(2, 1, 0), NDArrayIndex.all())
  println(b)
}