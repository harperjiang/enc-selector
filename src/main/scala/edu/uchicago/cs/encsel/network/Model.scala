package edu.uchicago.cs.encsel.network

import scala.collection.mutable.ArrayBuffer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

import scala.collection.mutable.HashSet
import scala.collection.mutable.HashMap

abstract class Model(is: Model*) {

  protected var inputs = new HashSet[Model]
  protected var outputs = new HashSet[Model]

  protected var readyInput = new HashSet[Model]
  protected var readyOutput = new HashSet[Model]

  private[network] var value: INDArray = null
  private[network] var grad: INDArray = null

  inputs ++= is
  inputs.foreach(_.addOutput(this))

  def addInput(input: Model) = inputs += input
  def addOutput(output: Model) = outputs += output

  def forward(source: Model): Unit = {
    // Wait for all input to be ready
    if (inputs.contains(source))
      readyInput += source

    if (readyInput.size == inputs.size) {
      this.value = compute
      outputs.foreach { _.forward(this) }
      readyInput.clear()
    }
  }

  def backward(source: Model): Unit = {
    if (outputs.contains(source)) {
      readyOutput += source
      this.grad = this.grad.add(source.grad)
    }
    if (readyOutput.size == outputs.size) {
      updateGrad
      inputs.foreach { _.backward(this) }
      readyOutput.clear()
    }
  }

  def compute: INDArray;
  def updateGrad: Unit;

}

class Input extends Model() {
  def setValue(value: INDArray) = this.value = value
  def compute = this.value
  def updateGrad = Unit
}

class Output(input: Model) extends Model(input) {
  def getValue() = this.value
  def setGrad(grad: INDArray): Unit = this.grad = grad
  def compute = null
  def updateGrad = { input.grad = this.grad }
}

class Add(x: Model, y: Model) extends Model(x, y) {
  protected val left = x
  protected val right = y

  def compute: INDArray = {
    // Always assume y is a smaller
    if (right.value.shape.sameElements(right.value.shape)) {
      left.value.addi(right.value)
    } else {
      left.value.addiColumnVector(right.value)
    }
  }

  def updateGrad: Unit = {
    // TODO Sum along dimension
    left.grad = this.grad
    right.grad = this.grad
  }
}

class DotMul(x: Model, y: Model) extends Model(x, y) {

  protected val left = x
  protected val right = y

  def compute: INDArray = left.value.muli(right.value)

  def updateGrad: Unit = {
    left.grad = this.grad.muli(right.value)
    right.grad = left.value.muli(this.grad)
  }
}

class Mul(x: Model, y: Model) extends Model(x, y) {
  protected val left = x
  protected val right = y

  def compute: INDArray = left.value.mul(right.value)
  def updateGrad: Unit = {}
}
