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

  protected val readyInput = new HashSet[Node]
  protected val readyOutput = new HashSet[Node]

  private[ndnn] var value: INDArray = _
  private[ndnn] var grad: INDArray = _

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

    if (readyInput.size == inputs.size) {
      this.value = compute
      outputs.foreach { _.forward(this) }
      readyInput.clear()
      // Clear gradient for backward
      grad = null
    }
  }

  def backward(source: Node, grad: INDArray): Unit = {
    source match {
      case ths if ths == this => {
        this.grad = grad.dup()
      }
      case out if outputs.contains(out) => {
        readyOutput += source
        this.grad match {
          case null => this.grad = grad.dup()
          case _ => this.grad.addi(grad)
        }
      }
    }
    grad.cleanup()

    if (readyOutput.size == outputs.size) {
      updateGrad.foreach(pair => pair._1.backward(this, pair._2))
      readyOutput.clear()
    }
  }

  def compute: INDArray
  def updateGrad: Map[Node, INDArray]
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

  def compute = source match {
    case Some(x) => x.value
    case None => this.value
  }

  def updateGrad = {
    source match {
      case Some(x) => Map((x, grad.dup()))
      case None => Map.empty[Node, INDArray]
    }
  }
}

class Param(n: String) extends Input(n) {
  var context = new HashMap[String, INDArray]()
  def this() = this("default_param")
}

class Add(left: Node, right: Node) extends Node(left, right) {

  def compute: INDArray = {
    // Always assume y is a smaller
    if (left.value.shape.product < right.value.shape.product)
      throw new IllegalArgumentException()
    left.value.add(Broadcast.broadcast(right.value, left.value.shape))
  }

  def updateGrad = {
    // Sum along dimension
    val diff = Broadcast.diff(left.value.shape, right.value.shape)
    var leftgrad = this.grad.dup()
    if (diff._1.length != 0)
      leftgrad = leftgrad.sum(diff._1: _*)
    var rightgrad = this.grad.dup()
    if (diff._2.length != 0)
      rightgrad = rightgrad.sum(diff._2: _*)
    Map((left, leftgrad), (right, rightgrad))
  }
}

class Mul(left: Node, right: Node) extends Node(left, right) {

  // Always assume y is smaller
  def compute: INDArray = {
    if (left.value.shape.product < right.value.shape.product)
      throw new IllegalArgumentException()
    left.value.mul(Broadcast.broadcast(right.value, left.value.shape))
  }

  def updateGrad = {
    val diff = Broadcast.diff(left.value.shape, right.value.shape)
    var leftgrad = this.grad.mul(Broadcast.broadcast(right.value, left.value.shape))
    if (diff._1.length > 0)
      leftgrad = leftgrad.sum(diff._1: _*)
    var rightgrad = this.grad.mul(left.value)
    if (diff._2.length > 0)
      rightgrad = rightgrad.sum(diff._2: _*)
    Map((left, leftgrad), (right, rightgrad))
  }
}

class DotMul(left: Node, right: Node) extends Node(left, right) {

  def compute: INDArray = left.value.mmul(right.value)

  def updateGrad = {
    Map((left, this.grad.mmul(right.value.transpose())),
      (right, left.value.transpose().mmul(this.grad)))
  }
}

class ReLU(input: Node) extends Node(input) {
  def compute: INDArray = Transforms.relu(input.value)

  def updateGrad = {
    Map((input, this.grad.mul(this.value.gt(0))))
  }
}

class LeakyReLU(input: Node) extends Node(input) {
  def compute: INDArray = Transforms.leakyRelu(input.value)

  def updateGrad = {
    val op = new org.nd4j.linalg.api.ops.impl.transforms.LeakyReLUDerivative(this.value)
    val derivative = Nd4j.getExecutioner.execAndReturn(op)
    Map((input, this.grad.mul(derivative)))
  }
}

class Sigmoid(input: Node) extends Node(input) {
  def compute: INDArray = {
    Transforms.sigmoid(input.value)
  }

  def updateGrad = {
    Map((input, grad.mul(value.mul(value.sub(1).negi()))))
  }
}

class Tanh(input: Node) extends Node(input) {
  def compute: INDArray = {
    Transforms.tanh(input.value)
  }

  def updateGrad = {
    val onelike = Nd4j.onesLike(grad)
    Map((input, onelike.subi(value.mul(value)).muli(grad)))
  }
}

class SoftMax(input: Node) extends Node(input) {

  def compute: INDArray = {
    Nd4j.getExecutioner.execAndReturn(
      new org.nd4j.linalg.api.ops.impl.transforms.SoftMax(input.value.dup()))
  }

  def updateGrad = {
    val gvdot = Broadcast.broadcast(this.value.mul(this.grad).sum(1), this.grad.shape)
    Map((input, this.value.mul(this.grad.sub(gvdot))))
  }
}

class Concat(left: Node, right: Node, idx: Int = 1) extends Node(left, right) {
  def compute: INDArray = {
    Nd4j.concat(idx, left.value, right.value)
  }

  def updateGrad = {
    val leftgrad = this.grad.get(NDArrayIndex.all(),
      NDArrayIndex.interval(0, left.value.shape()(1))).dup()
    val rightgrad = this.grad.get(NDArrayIndex.all(),
      NDArrayIndex.interval(left.value.shape()(1), this.value.shape()(1))).dup()
    Map((left, leftgrad), (right, rightgrad))
  }
}

/**
 * [B,1] [C,N] => [B,N]
 */
class Embed(idx: Node, map: Node) extends Node(idx, map) {
  def compute: INDArray = {
    map.value.get(new SpecifiedIndex(NDArrayUtil.toInts(idx.value): _*),
      NDArrayIndex.all())
  }

  def updateGrad = {
    val grad = Nd4j.zerosLike(map.value)
    grad.put(Array(new SpecifiedIndex(NDArrayUtil.toInts(idx.value): _*),
      NDArrayIndex.all()), this.grad)
    Map((map, grad))
  }
}

class Slice(input: Node, axis: Int, idx: Int) extends Node(input) {
  def compute: INDArray = {
    input.value.get(Index.index(input.value.shape.length, axis, idx): _*)
  }

  def updateGrad = {
    val grad = Nd4j.zerosLike(input.value)
    grad.put(Index.index(input.value.shape.length, axis, idx), this.grad)
    Map((input, grad))
  }
}

class NewAxis(input: Node, idx: Int) extends Node(input) {
  def compute: INDArray = {
    val idices = new ArrayBuffer[INDArrayIndex]()
    idices ++= (0 until idx).map(i => NDArrayIndex.all())
    idices += NDArrayIndex.newAxis()
    idices ++= (idx until input.value.shape.length).map(i => NDArrayIndex.all())
    input.value.get(idices: _*).dup()
  }

  def updateGrad = {
    val idices = new ArrayBuffer[INDArrayIndex]()
    idices ++= (0 until idx).map(i => NDArrayIndex.all())
    idices += NDArrayIndex.point(0)
    idices ++= (idx until input.value.shape.length).map(i => NDArrayIndex.all())
    Map((input, this.value.get(idices: _*)))
  }
}

class Reshape(input: Node, shape: Array[Int]) extends Node(input) {

  def compute: INDArray = {
    input.value.dup().reshape(shape: _*)
  }

  def updateGrad = {
    Map((input, grad.dup().reshape(input.value.shape(): _*)))
  }
}

