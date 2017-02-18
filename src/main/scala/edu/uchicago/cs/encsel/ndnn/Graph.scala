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

  val inputs = new ArrayBuffer[Input]
  val params = new ArrayBuffer[Param]
  var output: Node = null

  def param(shape: Array[Int]): Param = {
    val newparam = new Param()
    newparam.value = initPolicy.init(shape)
    params += newparam
    newparam
  }

  def input(shape: Array[Int]): Input = {
    val newinput = new Input()
    inputs += newinput
    newinput
  }

  def output(node: Node): Unit = { output = node }

  def forward() = inputs.foreach { _.forward(null) }

  def backward(expected: INDArray) = {
    val loss = lossFunction.loss(output.value, expected)
    output.grad = lossFunction.gradient
    output.backward(null)

    params.foreach { updatePolicy.update(_) }
  }
}