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
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastAddOp
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastMulOp
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastSubOp

trait NodeEnv {
  protected val nodeBuffer = new ArrayBuffer[Node]
  def attach(n: Node) = { nodeBuffer += n }

  def forward: Unit =
    nodeBuffer.foreach { _.forward }

  def backward: Unit =
    nodeBuffer.reverseIterator.foreach(_.backward)

  def param(n: String): Param = {
    val newparam = new Param(n)
    newparam.setEnv(this)
    newparam
  }

  def input(n: String): Input = {
    val newinput = new Input(n)
    newinput.setEnv(this)
    newinput
  }
}

abstract class Node(is: Node*) {

  private[ndnn] var value: INDArray = _
  private[ndnn] var grad: INDArray = _
  private[ndnn] var scratchPad: INDArray = _

  private[ndnn] var env: NodeEnv = null

  if (is.length > 0) {
    attachTo(is(0).env)
  }

  protected def attachTo(e: NodeEnv): Unit = {
    e.attach(this)
    this.env = e
  }

  def forward: Unit = {
    compute
    if (this.grad != null)
      this.grad.assign(0)
  }
  def backward: Unit = updateGrad

  def compute: Unit
  def updateGrad: Unit

  protected def initShape(shape: Array[Int]): Unit = {
    if (this.value == null || !this.value.shape.sameElements(shape)) {
      this.value = Nd4j.zeros(shape: _*)
    }
    if (this.grad == null || !this.grad.shape.sameElements(shape)) {
      this.grad = Nd4j.zerosLike(this.value)
    }
    if (this.scratchPad == null || !this.scratchPad.shape.sameElements(shape)) {
      this.scratchPad = Nd4j.zerosLike(this.value)
    }
  }

  protected def assignValue(in: INDArray): INDArray = {
    initShape(in.shape)
    this.value.assign(in)
  }
}

class Input(n: String) extends Node {
  val name = n

  def this() = this("default_input")

  def setEnv(e: NodeEnv): Unit = {
    this.env = e;
    e.attach(this)
  }

  protected var rawValue: Any = _

  def get[T] = this.rawValue.asInstanceOf[T]
  def set(value: Any) = {
    this.rawValue = value
    if (value.isInstanceOf[INDArray])
      this.value = value.asInstanceOf[INDArray]
    if (value.isInstanceOf[Array[Double]]) {
      this.value = Nd4j.create(value.asInstanceOf[Array[Double]])
    }
    if (value.isInstanceOf[Array[Int]]) {
      this.value = Nd4j.create(value.asInstanceOf[Array[Int]].map(_.toDouble))
    }
  }

  def compute: Unit = {
    if (this.value != null)
      assignValue(this.value)
  }
  def updateGrad: Unit = Unit
}

class Param(n: String) extends Input(n) {
  val context = new HashMap[String, INDArray]()
}

class Add(left: Node, right: Node) extends Node(left, right) {

  def compute: Unit = {
    // Always assume y is a smaller
    val axis = Broadcast.axis(left.value.shape, right.value.shape)

    assignValue(
      axis.length match {
        case eq if eq == left.value.shape.length => left.value.add(right.value)
        case gt if gt > 0 => Nd4j.getExecutioner.execAndReturn(
          new BroadcastAddOp(left.value, right.value, left.value.dup(), axis: _*))
      })

  }

  def updateGrad: Unit = {
    // Sum along dimension
    val diff = Broadcast.diff(left.value.shape, right.value.shape)
    left.grad.addi(this.grad)

    val rightgrad = diff._2.length match {
      case 0 => { this.grad }
      case _ => { this.grad.sum(diff._2: _*) }
    }
    right.grad.addi(rightgrad)
  }
}

class Mul(left: Node, right: Node) extends Node(left, right) {
  // Always assume y is smaller
  def compute: Unit = {

    val axis = Broadcast.axis(left.value.shape, right.value.shape)

    assignValue(
      axis.length match {
        case eq if eq == left.value.shape.length => left.value.mul(right.value)
        case gt if gt > 0 => Nd4j.getExecutioner.execAndReturn(
          new BroadcastMulOp(left.value, right.value, left.value.dup(), axis: _*))
      })

  }

  def updateGrad = {
    val axis = Broadcast.axis(left.value.shape, right.value.shape)
    val diff = Broadcast.diff(left.value.shape, right.value.shape)
    left.scratchPad.assign(this.grad)

    val leftmul = axis.length match {
      case eq if eq == left.value.shape.length =>
        left.scratchPad.muli(right.value)
      case _ => {
        val op = new BroadcastMulOp(left.scratchPad, right.value, left.scratchPad, axis: _*)
        Nd4j.getExecutioner.execAndReturn(op)
      }
    }
    left.grad.addi(leftmul)

    this.scratchPad.assign(this.grad)
    diff._2.length match {
      case 0 => right.grad.addi(this.scratchPad.muli(left.value))
      case _ => right.grad.addi(this.scratchPad.muli(left.value).sum(diff._2: _*))
    }
  }
}

class DotMul(left: Node, right: Node) extends Node(left, right) {

  def compute: Unit = {
    initShape(Array(left.value.shape()(0), right.value.shape()(1)))
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
    initShape(input.value.shape)
    Operations.relu(input.value, this.value)
  }

  def updateGrad = {
    this.scratchPad.assign(this.value)
    input.grad.addi(this.scratchPad.gti(0).muli(this.grad))
  }
}

class LeakyReLU(input: Node) extends Node(input) {
  def compute: Unit = {
    initShape(input.value.shape)
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
    initShape(input.value.shape)
    Operations.sigmoid(input.value, this.value)
  }

  def updateGrad = {
    this.scratchPad.assign(this.value)
    input.grad.addi(this.scratchPad.subi(1).negi().muli(this.value).muli(grad))
  }
}

class Tanh(input: Node) extends Node(input) {
  def compute: Unit = {
    initShape(input.value.shape)
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
    //Operations.softmax(this.value)
  }

  def updateGrad = {
    // TODO Softmax has many NOT in-place operations. 
    this.scratchPad.assign(this.value)
    val gvdot = this.scratchPad.muli(this.grad).sum(this.grad.shape.length - 1)
    input.scratchPad.assign(this.grad)
    val op = new BroadcastSubOp(input.scratchPad, gvdot, input.scratchPad,
      this.grad.shape.indices.dropRight(1): _*)
    input.grad.addi(Nd4j.getExecutioner.execAndReturn(op).muli(this.value))
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

class Collect(nodes: Node*) extends Node(nodes: _*) {
  def compute: Unit = {
    val valueList = nodes.map(n => {
      n.value.get((NDArrayIndex.newAxis() +: NDArrayIndex.allFor(n.value)): _*)
    })
    assignValue(Nd4j.concat(0, valueList.toArray: _*))
  }

  def updateGrad = {
    val total = this.grad.shape.length - 1
    nodes.zipWithIndex.foreach { n =>
      {
        n._1.grad = this.grad.get(Index.point(total + 1, 0, n._2): _*)
      }
    }
  }
}

/**
 * [B,1] [C,N] => [B,N]
 */
class Embed(idx: Node, map: Node) extends Node(idx, map) {

  var fwdmap: INDArray = null
  var bwdmap: INDArray = null

  def compute: Unit = {

    val indexval = idx match {
      case input: Input => input.get[Array[Int]]
      case _ => NDArrayUtil.toInts(idx.value)
    }
    val mapsize = map.value.shape()
    if (fwdmap == null || fwdmap.shape().sameElements(Array(indexval, mapsize(0)))) {
      fwdmap = Nd4j.zeros(indexval.length, mapsize(0))
    } else {
      fwdmap.assign(0)
    }
    indexval.zipWithIndex.foreach(p => fwdmap.putScalar(Array(p._2, p._1), 1))
    bwdmap = fwdmap.transpose()
    initShape(Array(indexval.length, mapsize(1)))
    assignValue(fwdmap.mmuli(map.value, this.value))
  }

  def updateGrad = {
    // Support only column vectors
    map.grad.addi(bwdmap.mmul(this.grad))
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
    //    initShape(shape.dropRight(1) :+ 1)
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

