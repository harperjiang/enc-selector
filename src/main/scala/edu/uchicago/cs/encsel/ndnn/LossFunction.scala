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
  def forClassification: Boolean
  def loss(actual: INDArray, expected: INDArray, fortest: Boolean = false): Double
  def gradient: INDArray
  def accuracy: Int
}

abstract class LossFunctionBase extends LossFunction {
  protected var grad: INDArray = null
  protected var acc: Int = -1

  def gradient = grad
  def accuracy = acc
}

class SquareLoss extends LossFunctionBase {

  def forClassification = false
  /**
   * @param actual		Shape [B, N]
   * @param	expected	Shape [B, N]
   * @return	The averaged squared difference of actual - expected
   */
  def loss(actual: INDArray, expected: INDArray, fortest: Boolean): Double = {
    val b = actual.shape()(0)

    if (!fortest) {
      // Compute Gradient
      grad = actual.sub(expected).mul(1d / b)
    }

    // Loss
    Transforms.pow(actual.sub(expected), 2).sum(1).meanNumber().doubleValue() / 2
  }

}

object SoftMaxLogLoss {
  val clip = 1e-12
}

class SoftMaxLogLoss extends LossFunctionBase {

  def forClassification = true
  /**
   * @param	actual		Probability of each label. This is an <code>INDArray</code>
   * 									of shape [B, N], where B is the batch size, N is the dimension
   * @param	expected	The ground truth label. This is an <code>INDArray</code> of
   * 									shape [B, 1]
   * @return 	mean of log loss of ground truth label in actual probability
   *
   */
  def loss(actual: INDArray, expected: INDArray, fortest: Boolean): Double = {
    val shape = actual.shape()
    val b = shape(0)
    val n = shape(1)

    val fetch = Indexer.get(actual, expected)
    val clipval = Transforms.max(fetch, SoftMaxLogLoss.clip)
    if (!fortest) {
      // Compute gradient
      grad = Nd4j.createUninitialized(b, n).assign(0)
      val allone = Nd4j.create((0 to b - 1).map(i => 1d / b).toArray).reshape(b, -1)

      // -log(x) gradient

      Indexer.put(grad, expected, allone.div(clipval).neg())
    } else if (forClassification) {
      // Accuracy for classification
      val predict = Nd4j.argMax(actual, 1)
      acc = predict.eq(expected).sumNumber().intValue()
    }
    Transforms.log(clipval).neg().meanNumber().doubleValue()
  }

}