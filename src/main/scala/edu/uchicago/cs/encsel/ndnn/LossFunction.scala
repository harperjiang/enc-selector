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

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.ops.transforms.Transforms
import org.nd4j.linalg.factory.Nd4j

trait LossFunction {
  def loss(actual: INDArray, expected: INDArray): Double
  def gradient: INDArray
}

class SigmoidLogLoss extends LossFunction {
  val clip = 1e-12
  var grad: INDArray = null
  /**
   * @param	actual		Probability of each label. This is an <code>INDArray</code>
   * 									of shape [B, N], where B is the batch size, N is the dimension
   * @param	expected	The ground truth label. This is an <code>INDArray</code> of
   * 									shape [B, 1]
   * @return 	mean of log loss of ground truth label in actual probability
   *
   */
  def loss(actual: INDArray, expected: INDArray): Double = {
    val shape = actual.shape()
    val b = shape(0)
    val n = shape(1)

    val flatten = actual.reshape(b * n, -1)
    val idxflatten = expected.reshape(b, -1)
    val offset = Nd4j.create((0 until b).map(_.doubleValue() * n).toArray).reshape(b, -1)
    idxflatten.addi(offset)

    // Compute gradient
    grad = Nd4j.createUninitialized(b * n, 1)
    grad.assign(0)
    val allone = Nd4j.create((0 to b - 1).map(i => 1d / b).toArray)
    grad.put(NDArrayIndex.allFor(idxflatten), allone)
    grad = grad.reshape(b, n)

    val fetch = flatten.get(NDArrayIndex.allFor(idxflatten): _*)

    Transforms.log(Transforms.max(fetch, clip)).neg().meanNumber().doubleValue()
  }

  /**
   *
   * @return the gradient of shape [B,N]. 1 for elements that are referenced, 0 otherwise
   */
  def gradient: INDArray = grad
}