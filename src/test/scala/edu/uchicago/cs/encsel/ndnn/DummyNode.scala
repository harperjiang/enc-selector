package edu.uchicago.cs.encsel.ndnn

/**
  * Created by Cathy on 2/18/2017.
  */
class DummyNode(ins:Node*) extends Node(ins:_*) {
  val first = ins(0)
  def compute = first.value
  def updateGrad = inputs.map((_, this.grad)).toMap
}
