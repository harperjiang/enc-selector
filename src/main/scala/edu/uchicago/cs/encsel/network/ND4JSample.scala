package edu.uchicago.cs.encsel.network

import org.nd4j.linalg.factory.Nd4j

/**
  * Created by harper on 2/17/17.
  */
object ND4JSample extends App {

  var a = Nd4j.create(Array(Array(1.0,2.0),Array(3.0,4.0)))
  println(a)
}
