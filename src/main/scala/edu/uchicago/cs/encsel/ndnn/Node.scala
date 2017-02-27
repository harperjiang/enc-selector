/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *
 * *****************************************************************************
 */
package edu.uchicago.cs.encsel.ndnn

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.INDArrayIndex
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.indexing.SpecifiedIndex
import org.nd4j.linalg.ops.transforms.Transforms
import org.nd4j.linalg.util.NDArrayUtil

abstract class Node(is: Node*) {

  protected val inputs = new HashSet[Node]
  protected val outputs = new HashSet[Node]

  protected var numConnectedOutput = 0
  protected val readyInput = new HashSet[Node]
  protected val readyOutput = new HashSet[Node]

  private[ndnn] var value: INDArray = _
  private[ndnn] var grad: INDArray = _
  private[ndnn] var scratchPad: INDArray = _

  // Dangling node will not have backprop
  var dangling = false

  {
    inputs ++= is
    inputs.foreach(_.addOutput(this))
  }

  def getInputs = inputs.clone()
  def getOutputs = outputs.clone()

  def addInput(input: Node) = inputs += input
  def addOutput(output: Node) = outputs += output

  def forward(source: Node): Unit = {
    // Wait for all input to be ready
    if (inputs.contains(source))
      readyInput += source

    if (outputs.contains(this)) {
      throw new RuntimeException();
    }
    if (readyInput.size == inputs.size) {
      compute
      numConnectedOutput = outputs.toList
        .map(_.dangling match {
          case true => 0
          case _ => 1
        }).sum
      outputs.foreach { _.forward(this) }
      readyInput.clear()
      // Clear gradient for backward
      if (grad != null)
        grad.assign(0)
    }
  }

  def backward(source: Node, grad: INDArray): Unit = {
    if (source != this)
      throw new IllegalArgumentException("Only for self backward grad assignment")
    source.grad = grad
    backward(this)
  }

  def backward(source: Node): Unit = {
    source match {
      case out if outputs.contains(out) => {
        readyOutput += source
      }
      case _ => {}
    }

    if (readyOutput.size == numConnectedOutput) {
      updateGrad
      inputs.foreach { _.backward(this) }
      readyOutput.clear()
    }
  }

  def compute: Unit
  def updateGrad: Unit

  protected def initValue(shape: Array[Int]) = {
    this.value match {
      case x if x != null && x.shape().sameElements(shape) => {}
      case _ => {
        this.value = Nd4j.createUninitialized(shape)
      }
    }
    if (this.grad == null)
      this.grad = Nd4j.createUninitialized(shape)
    if (this.scratchPad == null)
      this.scratchPad = Nd4j.createUninitialized(shape)
  }

  protected def assignValue(in: INDArray): INDArray = {
    initValue(in.shape)
    this.value.assign(in)
    this.value
  }
}

class Input(n: String, srcs: Node*) extends Node(srcs: _*) {
  val name = n

  {
    if (srcs.length > 1)
      throw new IllegalArgumentException("Input can have at most one source")
  }

  val source: Option[Node] = srcs.length match {
    case 1 => Some(srcs(0))
    case _ => None
  }

  def this() = this("default_input")
  def this(src: Node) = this("default_input", src)

  def getValue = this.value
  def setValue(value: INDArray) = this.value = value

  def compute: Unit = {
    source match {
      case Some(x) => assignValue(x.value)
      case None => { assignValue(this.value) }
    }
  }
  def updateGrad: Unit = {
    source match {
      case Some(x) => x.grad.addi(this.grad)
      case None => {}
    }
  }

  def forward: Unit = forward(this)
}

class Param(n: String) extends Input(n) {
  var context = new HashMap[String, INDArray]()
  def this() = this("default_param")
}

class Add(left: Node, right: Node) extends Node(left, right) {

  def compute: Unit = {
    // Always assume y is a smaller
    if (left.value.shape.product < right.value.shape.product)
      throw new IllegalArgumentException()
    assignValue(left.value).addi(Broadcast.broadcast(right.value, left.value.shape))
  }

  def updateGrad: Unit = {
    // Sum along dimension
    val diff = Broadcast.diff(left.value.shape, right.value.shape)
    left.grad.assign(this.grad)

    val rightgrad = diff._2.length match {
      case 0 => { this.grad }
      case _ => { this.grad.sum(diff._2: _*) }
    }
    right.grad.assign(rightgrad)
  }
}

class Mul(left: Node, right: Node) extends Node(left, right) {

  // Always assume y is smaller
  def compute: Unit = {
    if (left.value.shape.product < right.value.shape.product)
      throw new IllegalArgumentException()
    assignValue(left.value).muli(Broadcast.broadcast(right.value, left.value.shape))
  }

  def updateGrad = {
    val diff = Broadcast.diff(left.value.shape, right.value.shape)

    left.grad.assign(this.grad)
    left.grad.muli(Broadcast.broadcast(right.value, left.value.shape))

    diff._2.length match {
      case 0 => {
        right.grad.assign(this.grad)
        right.grad.muli(left.value)
      }
      case _ => {
        this.scratchPad.assign(this.grad)
        this.scratchPad.muli(left.value)
        right.grad.assign(this.scratchPad.sum(diff._2: _*))
      }
    }
  }
}

class DotMul(left: Node, right: Node) extends Node(left, right) {

  def compute: Unit = {
    initValue(Array(left.value.shape()(0), right.value.shape()(1)))
    assignValue(left.value.mmul(right.value, this.scratchPad))
  }

  def updateGrad = {
    right.scratchPad.assign(right.value)
    // In place transpose to reuse buffer
    right.scratchPad.permutei(1, 0)
    this.grad.mmul(right.scratchPad, left.scratchPad)
    left.grad.addi(left.scratchPad)

    left.scratchPad.assign(left.value)
    left.scratchPad.permutei(1, 0)
    right.scratchPad.permutei(1, 0)
    left.scratchPad.mmul(this.grad, right.scratchPad)
    left.scratchPad.permutei(1, 0)
    right.grad.addi(right.scratchPad)

  }
}

class ReLU(input: Node) extends Node(input) {
  def compute: Unit = {
    initValue(input.value.shape)
    Operations.relu(input.value, this.value)
  }

  def updateGrad = {
    this.scratchPad.assign(this.value)
    input.grad.addi(this.scratchPad.gti(0).muli(this.grad))
  }
}

class LeakyReLU(input: Node) extends Node(input) {
  def compute: Unit = {
    initValue(input.value.shape)
    Operations.leakyRelu(input.value, this.value)
  }

  def updateGrad = {
    val op = new org.nd4j.linalg.api.ops.impl.transforms.LeakyReLUDerivative(
      this.value, null, this.scratchPad, this.value.lengthLong())
    val derivative = Nd4j.getExecutioner.execAndReturn(op)
    input.grad.addi(this.scratchPad)
  }
}

class Sigmoid(input: Node) extends Node(input) {
  def compute: Unit = {
    initValue(input.value.shape)
    Operations.sigmoid(input.value, this.value)
  }

  def updateGrad = {
    this.scratchPad.assign(this.value)
    input.grad.addi(this.scratchPad.subi(1).negi().muli(this.value).muli(grad))
  }
}

class Tanh(input: Node) extends Node(input) {
  def compute: Unit = {
    initValue(input.value.shape)
    Operations.tanh(input.value, this.value)
  }

  def updateGrad = {
    input.scratchPad.assign(value)
    this.scratchPad.assign(1)
    input.grad.addi(this.scratchPad.subi(input.scratchPad.muli(value)).muli(grad))
  }
}

class SoftMax(input: Node) extends Node(input) {

  def compute: Unit = {
    assignValue(input.value)
    Operations.softmaxi(this.value)
  }

  def updateGrad = {
    // TODO Softmax has many NOT in-place operations. Considering optimization later
    this.scratchPad.assign(this.value)
    val gvdot = Broadcast.broadcast(this.scratchPad.muli(this.grad).sum(1), this.grad.shape)
    input.scratchPad.assign(this.grad)
    input.grad.addi(input.scratchPad.sub(gvdot).muli(this.value))
  }
}

class Concat(left: Node, right: Node, idx: Int = 1) extends Node(left, right) {
  def compute: Unit = {
    // TODO Not in-place
    assignValue(Nd4j.concat(idx, left.value, right.value))
  }

  def updateGrad = {
    left.grad.addi(this.grad.get(NDArrayIndex.all(),
      NDArrayIndex.interval(0, left.value.shape()(1))))
    right.grad.addi(this.grad.get(NDArrayIndex.all(),
      NDArrayIndex.interval(left.value.shape()(1), this.value.shape()(1))))
  }
}

/**
 * [B,1] [C,N] => [B,N]
 */
class Embed(idx: Node, map: Node) extends Node(idx, map) {

  def compute: Unit = {
    assignValue(map.value.get(new SpecifiedIndex(NDArrayUtil.toInts(idx.value): _*),
      NDArrayIndex.all()))
  }

  def updateGrad = {
    // Support only column vectors
    val idxval = idx.value.isRowVector() match {
      case true => idx.value.transpose()
      case false => idx.value
    }
    idx.scratchPad.assign(1)
    val permu = Nd4j.zeros(this.value.shape()(0), map.value.shape()(0))
    Index.put(permu, idxval, idx.scratchPad)

    permu.permutei(1, 0).mmul(this.grad, map.scratchPad)
    map.grad.addi(map.scratchPad)
  }
}

/**
 * Fetch a slice on the given index
 */
class Slice(input: Node, axis: Int, idx: Int) extends Node(input) {
  def compute: Unit = {
    assignValue(input.value.get(Index.point(input.value.shape.length, axis, idx): _*))
  }

  def updateGrad = {
    input.scratchPad.assign(0)
    input.scratchPad.put(Index.point(input.value.shape.length, axis, idx), this.grad)
    input.grad.addi(input.scratchPad)
  }
}

class ArgMax(input: Node) extends Node(input) {

  def compute: Unit = {
    // Shape is [A,B,C,...,1]
    // TODO Not in-place
    //    val shape = this.input.value.shape()
    //    initValue(shape.dropRight(1) :+ 1)
    assignValue(Nd4j.argMax(input.value, 1))
  }

  def updateGrad = {
    input.scratchPad.assign(0)
    Index.put(input.scratchPad, this.value, this.grad)
    input.grad.addi(input.scratchPad)
  }
}

//class NewAxis(input: Node, idx: Int) extends Node(input) {
//  def compute: Unit = {
//    val idices = new ArrayBuffer[INDArrayIndex]()
//    idices ++= (0 until idx).map(i => NDArrayIndex.all())
//    idices += NDArrayIndex.newAxis()
//    idices ++= (idx until input.value.shape.length).map(i => NDArrayIndex.all())
//    assignValue(input.value.get(idices: _*))
//  }
//
//  def updateGrad = {
//    val idices = new ArrayBuffer[INDArrayIndex]()
//    idices ++= (0 until idx).map(i => NDArrayIndex.all())
//    idices += NDArrayIndex.point(0)
//    idices ++= (idx until input.value.shape.length).map(i => NDArrayIndex.all())
//    Map((input, this.value.get(idices: _*)))
//  }
//}

//class Reshape(input: Node, shape: Array[Int]) extends Node(input) {
//
//  def compute: Unit = assignValue(input.value.reshape(shape: _*))
//
//  def updateGrad = {
//    Map((input, grad.reshape(input.value.shape(): _*)))
//  }
//}

