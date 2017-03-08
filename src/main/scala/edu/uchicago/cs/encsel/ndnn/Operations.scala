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
import org.nd4j.linalg.api.ops.impl.transforms.RectifedLinear
import org.nd4j.linalg.factory.Nd4j

/**
 * The operations in this class supports in-place assignment with a given destination
 */
object Operations {



  def relu(from: INDArray, to: INDArray): INDArray = {
    Nd4j.getExecutioner().execAndReturn(new RectifedLinear(from, null, to, from.lengthLong()))
  }

  def leakyRelu(from: INDArray, to: INDArray): INDArray = {
    Nd4j.getExecutioner().execAndReturn(
      new org.nd4j.linalg.api.ops.impl.transforms.LeakyReLU(from, null, to, from.lengthLong()))
  }

  def sigmoid(from: INDArray, to: INDArray): INDArray = {
    Nd4j.getExecutioner().execAndReturn(
      new org.nd4j.linalg.api.ops.impl.transforms.Sigmoid(from, null, to, from.lengthLong()))
  }

  def tanh(from: INDArray, to: INDArray): INDArray = {
    Nd4j.getExecutioner().execAndReturn(
      new org.nd4j.linalg.api.ops.impl.transforms.Tanh(from, null, to, from.lengthLong()))
  }

  def softmax(in: INDArray): INDArray = {
    // It's weird that in-place softmax has bugs
    Nd4j.getExecutioner().execAndReturn(
      new org.nd4j.linalg.api.ops.impl.transforms.SoftMax(in))
  }
}