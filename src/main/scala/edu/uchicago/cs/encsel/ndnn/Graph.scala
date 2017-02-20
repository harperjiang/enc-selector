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

class Graph(ip: InitPolicy, up: UpdatePolicy, loss: LossFunction) {

  val initPolicy = ip
  val updatePolicy = up
  val lossFunction = loss

  protected val inputs = new ArrayBuffer[Input]
  protected val params = new ArrayBuffer[Param]
  protected val expected = new Input()
  protected var output: Node = null

  build

  def build: Unit = Unit

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
  def setOutput(node: Node): Unit = { output = node }

  def getInputs = inputs.clone()
  def getParams = params.clone()
  def getOutput = output

  def train: Double = {
    // TODO Validate the network

    // Forward
    inputs.foreach { input => input.forward(input) }
    params.foreach { p => p.forward(p) }
    // Compute Loss
    val loss = lossFunction.loss(output.value, expected.value)
    // Backward
    output.backward(output, lossFunction.gradient)
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
    val loss = lossFunction.loss(output.value, expected.value, false)

    (loss, lossFunction.accuracy)
  }
}