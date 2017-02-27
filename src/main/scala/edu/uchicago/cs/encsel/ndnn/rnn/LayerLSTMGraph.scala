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
package edu.uchicago.cs.encsel.ndnn.rnn

import scala.collection.mutable.ArrayBuffer

import edu.uchicago.cs.encsel.ndnn.Embed
import edu.uchicago.cs.encsel.ndnn.Graph
import edu.uchicago.cs.encsel.ndnn.Input
import edu.uchicago.cs.encsel.ndnn.Param
import edu.uchicago.cs.encsel.ndnn.SGD
import edu.uchicago.cs.encsel.ndnn.SoftMaxLogLoss
import edu.uchicago.cs.encsel.ndnn.Xavier
import edu.uchicago.cs.encsel.ndnn.Zero
import edu.uchicago.cs.encsel.ndnn.SoftMax
import edu.uchicago.cs.encsel.ndnn.DotMul
import edu.uchicago.cs.encsel.ndnn.ArgMax

class LayerLSTMGraph(layer: Int, numChar: Int, hiddenDim: Int, len: Int, prefixlength: Int = -1)
    extends Graph(Xavier, new SGD(0.05, 1), new SoftMaxLogLoss) {

  val length = len

  val c2v = param("c2v", Array(numChar, hiddenDim))
  val v2c = param("v2c", Array(hiddenDim, numChar))
  val wf = new Array[Param](layer)
  val bf = new Array[Param](layer)
  val wi = new Array[Param](layer)
  val bi = new Array[Param](layer)
  val wc = new Array[Param](layer)
  val bc = new Array[Param](layer)
  val wo = new Array[Param](layer)
  val bo = new Array[Param](layer)

  val h0 = new Array[Input](layer)
  val c0 = new Array[Input](layer)
  val xs = new ArrayBuffer[Input]()

  val cells = new ArrayBuffer[Array[LSTMCell]]()

  {
    val inputSize = 2 * hiddenDim
    for (i <- 0 until layer) {
      wf(i) = param("wf_%d".format(i), Array(inputSize, hiddenDim))
      bf(i) = param("bf_%d".format(i), Array(1, hiddenDim))(Zero)
      wi(i) = param("wi_%d".format(i), Array(inputSize, hiddenDim))
      bi(i) = param("bi_%d".format(i), Array(1, hiddenDim))(Zero)
      wc(i) = param("wc_%d".format(i), Array(inputSize, hiddenDim))
      bc(i) = param("bc_%d".format(i), Array(1, hiddenDim))(Zero)
      wo(i) = param("wo_%d".format(i), Array(inputSize, hiddenDim))
      bo(i) = param("bo_%d".format(i), Array(1, hiddenDim))(Zero)
      h0(i) = input("h0_%d".format(i))
      c0(i) = input("c0_%d".format(i))
    }
  }

  extend(length)

  // Extend RNN to the expected size and build connections between cells
  def extend(length: Int): Unit = {
    // Expand the network
    for (i <- cells.length until length) {
      val newNodes = new Array[LSTMCell](layer)
      for (j <- 0 until layer) {
        val h = i match { case 0 => h0(j) case x => cells(i - 1)(j).hout }
        val c = i match { case 0 => c0(j) case x => cells(i - 1)(j).cout }
        val in = j match {
          case 0 => {
            val in =
              i match {
                case gt if prefixlength > 0 && gt >= prefixlength => {
                  val pred = new ArgMax(outputs(i - 1))
                  input("%d".format(i), pred)
                }
                case _ => input("%d".format(i))
              }
            val mapped = new Embed(in, c2v)
            xs += in
            mapped
          } case x => newNodes(j - 1).hout
        }
        newNodes(j) = new LSTMCell(wf(j), bf(j), wi(j), bi(j),
          wc(j), bc(j), wo(j), bo(j), in, h, c)
      }
      output(new SoftMax(new DotMul(newNodes(layer - 1).hout, v2c)))
      cells += newNodes
    }
  }
}