package edu.uchicago.cs.encsel.network

import scala.collection.mutable.ArrayBuffer
import org.nd4j.linalg.api.ndarray.INDArray
import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

abstract class Model(is: Model*) {

  protected var inputs = new HashSet[Model]
  protected var outputs = new HashSet[Model]

  protected var readyInput = new HashMap[Model, INDArray]
  protected var readyOutput = new HashMap[Model, INDArray]

  protected var value: INDArray = null
  protected var grad: INDArray = null

  inputs ++= is
  inputs.foreach(_.addOutput(this))

  def addInput(input: Model) = inputs += input
  def addOutput(output: Model) = outputs += output

  def forward(source: Model, value: INDArray): Unit = {
    // Wait for all input to be ready
    if (inputs.contains(source))
      readyInput += ((source, value))

    if (readyInput.size == inputs.size) {
      this.value = compute
      outputs.foreach { _.forward(this, value) }
      readyInput.clear()
    }
  }

  def backward(source: Model, bgrad: INDArray): Unit = {
    if (outputs.contains(source))
      readyOutput += ((source, bgrad))
    if (readyOutput.size == outputs.size) {
      grad = computeGrad
      inputs.foreach { _.backward(this, grad) }
      readyOutput.clear()
    }
  }

  def compute: INDArray;
  def computeGrad: INDArray;

}

class Input extends Model() {
  
}

class Output(input:Model) extends Model(input) {
  
}