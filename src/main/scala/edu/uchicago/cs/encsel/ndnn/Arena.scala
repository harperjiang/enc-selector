package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.indexing.SpecifiedIndex
import scala.collection.mutable.ArrayBuffer

object Arena extends App {

  // Test ND4J performance of copying and assign

  var value = Nd4j.zeros(1000, 1000)
  val a = Nd4j.ones(1000, 1000)
  val b = Nd4j.ones(1000, 1000)
  for (i <- 0 to 1000000) {
    a.assign(i % 79)
    b.assign(i % 211)
    value.assign(a)
    value.addi(b)
    if (i % 1000 == 0)
      println(i)
  }

}