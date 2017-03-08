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
  * Hao Jiang - initial API and implementation
  *
  * *****************************************************************************
  */
package edu.uchicago.cs.encsel.ndnn.rnn

import scala.collection.mutable.ArrayBuffer

import edu.uchicago.cs.encsel.ndnn.ArgMax
import edu.uchicago.cs.encsel.ndnn.Collect
import edu.uchicago.cs.encsel.ndnn.DotMul
import edu.uchicago.cs.encsel.ndnn.Embed
import edu.uchicago.cs.encsel.ndnn.Graph
import edu.uchicago.cs.encsel.ndnn.Input
import edu.uchicago.cs.encsel.ndnn.Node
import edu.uchicago.cs.encsel.ndnn.SGD
import edu.uchicago.cs.encsel.ndnn.SoftMax
import edu.uchicago.cs.encsel.ndnn.SoftMaxLogLoss
import edu.uchicago.cs.encsel.ndnn.Xavier
import edu.uchicago.cs.encsel.ndnn.Zero

class LSTMGraph(numChar: Int, hiddenDim: Int)
  extends Graph[Array[Array[Int]]](Xavier, new SGD(0.5, 0.95, 10), new LSTMLoss) {

  val c2v = param("c2v", Array(numChar, hiddenDim))
  val v2c = param("v2c", Array(hiddenDim, numChar))
  val inputSize = 2 * hiddenDim

  val wf = param("wf", Array(inputSize, hiddenDim))
  val bf = param("bf", Array(1, hiddenDim))(Zero)
  val wi = param("wi", Array(inputSize, hiddenDim))
  val bi = param("bi", Array(1, hiddenDim))(Zero)
  val wc = param("wc", Array(inputSize, hiddenDim))
  val bc = param("bc", Array(1, hiddenDim))(Zero)
  val wo = param("wo", Array(inputSize, hiddenDim))
  val bo = param("bo", Array(1, hiddenDim))(Zero)

  val h0 = input("h0")
  val c0 = input("c0")

  val nodeWatermark = nodeBuffer.length

  val xs = new ArrayBuffer[Input]()

  protected def clean: Unit = {
    // Remove all nodes above water mark
    nodeBuffer.remove(nodeWatermark, nodeBuffer.length - nodeWatermark)
    xs.clear
    // Leave h0 and c0
    inputs.remove(2, inputs.length - 2)
  }

  def build(length: Int): Unit = {
    clean

    val collected = new ArrayBuffer[Node]()
    // Extend RNN to the expected size and build connections between cells
    var h: Node = h0
    var c: Node = c0
    for (i <- 0 until length) {
      val in = input("%d".format(i))
      val mapped = new Embed(in, c2v)
      xs += in

      val newNode = LSTMCell.build(wf, bf, wi, bi,
        wc, bc, wo, bo, mapped, h, c)
      collected += new SoftMax(new DotMul(newNode._2, v2c))
      h = newNode._2
      c = newNode._1
    }
    output(new Collect(collected: _*))
  }
}

class LSTMPredictGraph(numChar: Int, hiddenDim: Int)
  extends LSTMGraph(numChar, hiddenDim) {

  def build(length: Int, predictLength: Int): Unit = {
    clean

    var h : Node = h0
    var c : Node = c0
    // Extend RNN to the expected size and build connections between cells
    for (i <- 0 until length) {

      val in = i match {
        case gt if gt >= predictLength => {
          new ArgMax(new SoftMax(new DotMul(h, v2c)))
        }
        case _ => {
          val realin = input("%d".format(i))
          xs += realin
          realin
        }
      }
      val mapped = new Embed(in, c2v)

      val newNode = LSTMCell.build(wf, bf, wi, bi,
        wc, bc, wo, bo, mapped, h, c)
      c = newNode._1
      h = newNode._2
    }
  }
}