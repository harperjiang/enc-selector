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

import scala.collection.mutable.HashSet

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.api.ops.TransformOp
import org.nd4j.linalg.factory.Nd4j
import scala.collection.mutable.HashMap

object Node {
  /**
   * Broadcast a to the given shape.
   *
   * This method support broadcast from lower dimension to higher dimension.
   * E.g., [a,b] -> [x,y,a,b], but no [a,b]-> [a,b,c,d]
   * 1 can be broadcasted to bigger numbers, e.g., [1,b]->[c,a,b]
   */
  def broadcast(a: INDArray, shape: Array[Int]): INDArray = {
    var originShape = a.shape()

    if (originShape.sameElements(shape))
      return a
    val originProd = originShape.product
    val shapeProd = shape.product
    if (originProd > shapeProd || shapeProd % originProd != 0)
      throw new IllegalArgumentException("Cannot broadcast, [%d]->[%d]".format(originProd, shapeProd))

    var newProd = 1

    var lengthDiff = shape.length - originShape.length
    for (i <- shape.length - 1 to 0 by -1) {
      var offsetIdx = i - lengthDiff
      if (offsetIdx >= 0 && originShape(offsetIdx) != 1 && shape(i) != originShape(offsetIdx)) {
        throw new IllegalArgumentException("Different shape: %d@%d<->%d@%d"
          .format(shape(i), i, originShape(offsetIdx), offsetIdx))
      }
      if (offsetIdx < 0 || originShape(offsetIdx) == 1) {
        newProd *= shape(i)
      }
    }
    a.reshape(1, -1).broadcast(newProd, originProd).reshape(shape: _*)
  }

  /**
   * Assume the arrays are broadcast-able. Compute the different axis
   */
  def diff(ashape: Array[Int], bshape: Array[Int]): (Array[Int], Array[Int]) = {
    val maxlen = Math.max(ashape.length, bshape.length)
    val apadded = ashape.reverse.padTo(maxlen, 0).reverse
    val bpadded = bshape.reverse.padTo(maxlen, 0).reverse
    val maxdim = apadded.zipAll(bpadded, 0, 0).map(p => Math.max(p._1, p._2))

    (apadded.zipAll(maxdim, 0, 0).zipWithIndex
      .filter(p => p._1._1 < p._1._2).map(_._2),
      bpadded.zipAll(maxdim, 0, 0).zipWithIndex
      .filter(p => p._1._1 < p._1._2).map(_._2))
  }
}

abstract class Node(is: Node*) {

  protected var inputs = new HashSet[Node]
  protected var outputs = new HashSet[Node]

  protected var readyInput = new HashSet[Node]
  protected var readyOutput = new HashSet[Node]

  private[ndnn] var value: INDArray = null
  private[ndnn] var grad: INDArray = null

  inputs ++= is
  inputs.foreach(_.addOutput(this))

  def addInput(input: Node) = inputs += input
  def addOutput(output: Node) = outputs += output

  def forward(source: Node): Unit = {
    // Wait for all input to be ready
    if (source != null && inputs.contains(source))
      readyInput += source

    if (readyInput.size == inputs.size) {
      this.value = compute
      outputs.foreach { _.forward(this) }
      readyInput.clear()
    }
  }

  def backward(source: Node): Unit = {
    if (source != null && outputs.contains(source)) {
      readyOutput += source
      this.grad.addi(source.grad)
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

abstract class OpNode(op: TransformOp, input: Node) extends Node(input) {

  def compute = {
    op.init(input.value, null, input.value.dup(), input.value.lengthLong())
    Nd4j.getExecutioner.execAndReturn(op)
  }

  def updateGrad = {
    val derivative = op.derivative()
    input.grad = this.grad.mul(Nd4j.getExecutioner.execAndReturn(derivative))
  }
}

class Input extends Node {
  def setValue(value: INDArray) = this.value = value
  def compute = this.value
  def updateGrad = Unit
}

class Param extends Input {
  var context = new HashMap[String, INDArray]()

}

class Add(x: Node, y: Node) extends Node(x, y) {
  protected val left = x
  protected val right = y

  def compute: INDArray = {
    // Always assume y is a smaller
    if (left.value.shape.product < right.value.shape.product)
      throw new IllegalArgumentException()
    left.value.add(Node.broadcast(right.value, left.value.shape))
  }

  def updateGrad: Unit = {
    // Sum along dimension
    val diff = Node.diff(left.value.shape, right.value.shape)
    left.grad = this.grad.dup()
    if (diff._1.length != 0)
      left.grad = left.grad.sum(diff._1: _*)
    right.grad = this.grad.dup()
    if (diff._2.length != 0)
      right.grad = right.grad.sum(diff._2: _*)
  }
}

class Mul(x: Node, y: Node) extends Node(x, y) {
  protected val left = x
  protected val right = y

  // Always assume y is smaller
  def compute: INDArray = {
    if (left.value.shape.product < right.value.shape.product)
      throw new IllegalArgumentException()
    left.value.mul(Node.broadcast(right.value, left.value.shape))
  }

  def updateGrad: Unit = {
    val diff = Node.diff(left.value.shape, right.value.shape)
    left.grad = this.grad.mul(Node.broadcast(right.value, left.value.shape))
    if (diff._1.length > 0)
      left.grad = left.grad.sum(diff._1: _*)
    right.grad = this.grad.mul(left.value)
    if (diff._2.length > 0)
      right.grad = right.grad.sum(diff._2: _*)
  }
}

class DotMul(x: Node, y: Node) extends Node(x, y) {

  protected val left = x
  protected val right = y

  def compute: INDArray = left.value.mmul(right.value)

  def updateGrad: Unit = {
    left.grad = this.grad.mmul(right.value.transpose())
    right.grad = left.value.transpose().mmul(this.grad)
  }
}

class Sigmoid(input: Node) extends OpNode(new org.nd4j.linalg.api.ops.impl.transforms.Sigmoid(), input);

class SoftMax(input: Node) extends OpNode(new org.nd4j.linalg.api.ops.impl.transforms.SoftMax(), input)
    
    
