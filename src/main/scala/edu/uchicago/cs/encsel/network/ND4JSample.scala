package edu.uchicago.cs.encsel.network

import org.nd4j.linalg.factory.Nd4j

/**
 * Created by harper on 2/17/17.
 */
object ND4JSample extends App {

  var a = Nd4j.createUninitialized(Array(3,2,4,4))
  var b = Nd4j.createUninitialized(Array(2,4,4))
  
  a.add(b.broadcast(3))
}
