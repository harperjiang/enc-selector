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

class LSTMGraph(numChar: Int, hiddenDim: Int, len: Int, prefixlength: Int = -1)
    extends Graph(Xavier, new SGD(0.5, 0.95), new SoftMaxLogLoss) {

  val length = len

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
  val xs = new ArrayBuffer[Input]()

  val cells = new ArrayBuffer[LSTMCell]()

  {
    // Extend RNN to the expected size and build connections between cells
    for (i <- 0 until length) {
      val h = i match { case 0 => h0 case x => cells(i - 1).hout }
      val c = i match { case 0 => c0 case x => cells(i - 1).cout }
      val in = i match {
        case gt if prefixlength > 0 && gt >= prefixlength => {
          val pred = new ArgMax(outputs(i - 1))
          input("%d".format(i), pred)
        }
        case _ => input("%d".format(i))
      }
      val mapped = new Embed(in, c2v)
      xs += in

      val newNode = new LSTMCell(wf, bf, wi, bi,
        wc, bc, wo, bo, mapped, h, c)
      output(new SoftMax(new DotMul(newNode.hout, v2c)))
      cells += newNode
    }
  }
}