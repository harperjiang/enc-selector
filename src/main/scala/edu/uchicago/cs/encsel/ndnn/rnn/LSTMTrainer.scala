/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License,
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *
 */

package edu.uchicago.cs.encsel.ndnn.rnn

import org.nd4j.linalg.factory.Nd4j
import edu.uchicago.cs.encsel.ndnn.Trainer
import edu.uchicago.cs.encsel.ndnn.Evaluator
import edu.uchicago.cs.encsel.ndnn.FileStore
import edu.uchicago.cs.encsel.ndnn.Batch
import edu.uchicago.cs.encsel.ndnn.SimpleTrainer

class LSTMEvaluator extends Evaluator {

  protected var batchCounter = 0
  protected var charCounter = 0
  protected var lossSum = 0d
  protected var accSum = 0

  override def init = {
    batchCounter = 0
    charCounter = 0
    lossSum = 0
    accSum = 0
  }

  override def record[D](batch: Batch[D], loss: Double, acc: Int) = {
    val lstmbatch = batch.asInstanceOf[Batch[Array[Array[Int]]]]
    batchCounter += 1
    charCounter += lstmbatch.data.length * batch.size
    lossSum += loss
    accSum += acc
  }

  override def loss = lossSum / batchCounter

  override def summary = {
    "Average loss %f, prediction accuracy %f".format(lossSum / batchCounter, accSum.toDouble / charCounter)
  }
}

class LSTMTrainer(ts: LSTMDataset, tsts: LSTMDataset, g: LSTMGraph)
    extends SimpleTrainer[Array[Array[Int]], LSTMDataset, LSTMGraph](ts, tsts, g) {

  paramStore = new FileStore("LSTM_model.mdl")
  evaluator = new LSTMEvaluator()

  def setupGraph(g: LSTMGraph, batch: Batch[Array[Array[Int]]]) = {
    g.build(batch.data.length)

    // Setup x_i input
    batch.data.zip(g.xs).foreach(pair => pair._2.set(pair._1))
    // Setup h_0 and c_0
    g.h0.set(Nd4j.zeros(batch.size, g.hiddenDimension))
    g.c0.set(Nd4j.zeros(batch.size, g.hiddenDimension))
    // Expect
    g.expect(batch.groundTruth)
  }

}