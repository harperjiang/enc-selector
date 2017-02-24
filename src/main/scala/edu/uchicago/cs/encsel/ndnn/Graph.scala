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
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.api.ndarray.INDArray
import scala.collection.GenIterable

class Graph(ip: InitPolicy, up: UpdatePolicy, loss: LossFunction) {

  val initPolicy = ip
  val updatePolicy = up
  val lossFunction = loss

  protected val inputs = new ArrayBuffer[Input]
  protected val params = new ArrayBuffer[Param]
  protected val expected = new Input()
  protected val outputs = new ArrayBuffer[Node]

  def param(n: String, shape: Array[Int])(implicit usePolicy: InitPolicy = initPolicy): Param = {
    val newparam = new Param(n)
    newparam.value = usePolicy.init(shape)
    params += newparam
    newparam
  }

  def input(n: String): Input = {
    val newinput = new Input(n)
    inputs += newinput
    newinput
  }

  def expect(value: INDArray) = expected.setValue(value)
  def output(node: Node): Unit = outputs += node

  def getInputs = inputs.clone()
  def getParams = params.clone()
  def getOutputs = outputs.clone()
  def getOutput = outputs.length match {
    case 0 => null
    case _ => outputs(0)
  }

  def train: Double = {
    // TODO Validate the network

    // Forward
    inputs.foreach { input => input.forward(input) }
    params.foreach { p => p.forward(p) }
    // Compute Loss
    val loss = outputs.length match {
      case gt if gt >= 1 => lossFunction.loss(outputs.map(_.value).toArray, expected.value)
      case _ => throw new IllegalArgumentException("No output")
    }

    // Backward
    outputs.zip(lossFunction.gradient).foreach(pair => pair._1.backward(pair._1, pair._2))
    // Update Parameters and Decay Weight
    params.foreach { updatePolicy.update(_) }
    updatePolicy.weightDecay()
    loss
  }

  def test: (Double, Int) = {
    // Forward
    inputs.foreach { input => input.forward(input) }
    params.foreach { p => p.forward(p) }
    // Compute Loss
    val loss = lossFunction.loss(outputs.map(_.value).toArray, expected.value, true)

    (loss, lossFunction.accuracy)
  }

  def dump(): Array[INDArray] = this.params.map { _.value }.toArray
  def load(params: GenIterable[INDArray]) = this.params.zip(params).foreach(p => p._1.value = p._2)
}