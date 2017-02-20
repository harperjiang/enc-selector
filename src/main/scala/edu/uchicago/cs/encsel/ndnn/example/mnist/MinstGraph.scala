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
package edu.uchicago.cs.encsel.ndnn.example.mnist

import edu.uchicago.cs.encsel.ndnn.Graph
import edu.uchicago.cs.encsel.ndnn.SGD
import edu.uchicago.cs.encsel.ndnn.Xavier
import edu.uchicago.cs.encsel.ndnn.SoftMaxLogLoss
import edu.uchicago.cs.encsel.ndnn.DotMul
import edu.uchicago.cs.encsel.ndnn.SoftMax
import edu.uchicago.cs.encsel.ndnn.Zero
import edu.uchicago.cs.encsel.ndnn.Add
import edu.uchicago.cs.encsel.ndnn.Input

class MinstGraph extends Graph(Xavier, new SGD(0.05, 1), new SoftMaxLogLoss) {

  def pixelInput = inputs(0)
  def w1 = params(0)
  def b1 = params(1)
  def w2 = params(2)
  def b2 = params(3)

  override def build: Unit = {
    val pixelInput = input("pixel")
    val w1 = param("w1", Array(28 * 28, 128))
    val b1 = param("b1", Array(1, 128))(Zero)
    val w2 = param("w2", Array(128, 10))
    val b2 = param("b2", Array(1, 10))(Zero)

    val wx = new DotMul(pixelInput, w1)
    val addb = new Add(wx, b1)
    val layer2 = new DotMul(addb, w2)
    val addb2 = new Add(layer2, b2)
    val softmax = new SoftMax(addb2)
    setOutput(softmax)
  }
}