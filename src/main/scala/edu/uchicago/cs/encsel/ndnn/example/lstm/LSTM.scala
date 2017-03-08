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
 *            Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.encsel.ndnn.example.lstm

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.api.ops.impl.broadcast.BroadcastAddOp
import org.nd4j.linalg.api.rng.distribution.impl.UniformDistribution
import org.nd4j.linalg.factory.Nd4j

import scala.util.Random
import edu.uchicago.cs.encsel.ndnn.{ Batch, Evaluator, Trainer, Xavier }
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph
import edu.uchicago.cs.encsel.ndnn.FileStore

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

class LSTMTrainer(ts: LSTMDataset, tsts: LSTMDataset, hiddenDim: Int)
    extends Trainer[Array[Array[Int]], LSTMDataset, LSTMGraph] {

  protected val graph = new LSTMGraph(ts.numChars, hiddenDim)
  protected val paramStore = new FileStore("LSTM_model.mdl")
  protected val evaluator = new LSTMEvaluator()

  def getTrainSet: LSTMDataset = ts
  def getTestSet: LSTMDataset = tsts
  def getGraph: LSTMGraph = graph

  override protected def getEvaluator = evaluator
  override protected def getParamStore = paramStore

  def setupGraph(g: LSTMGraph, batch: Batch[Array[Array[Int]]]) = {
    g.build(batch.data.length)

    // Setup x_i input
    batch.data.zip(graph.xs).foreach(pair => pair._2.set(pair._1))
    // Setup h_0 and c_0
    g.h0.set(Nd4j.zeros(batch.size, hiddenDim))
    g.c0.set(Nd4j.zeros(batch.size, hiddenDim))
    // Expect
    g.expect(batch.groundTruth)
  }

  override def evaluate(testBatchSize: Int) = {
    super.evaluate(testBatchSize)
    // TODO Do a sentence prediction
  }
}

object LSTM extends App {

  val hiddenDim = 200
  val batchSize = 50

  val trainds = new LSTMDataset("/home/harper/dataset/lstm/ptb.train.txt")
  val testds = new LSTMDataset("/home/harper/dataset/lstm/ptb.valid.txt")(trainds)

  val trainer = new LSTMTrainer(trainds, testds, hiddenDim)

  val graph = trainer.getGraph
  graph.c2v.value = Nd4j.readNumpy("/home/harper/dataset/numpy/c2v.npy")
  graph.wf.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wf.npy")
  graph.bf.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bf.npy")
  graph.wi.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wi.npy")
  graph.bi.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bi.npy")
  graph.wc.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wc.npy")
  graph.bc.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bc.npy")
  graph.wo.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wo.npy")
  graph.bo.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bo.npy")
  graph.v2c.value = Nd4j.readNumpy("/home/harper/dataset/numpy/v.npy")
  
  trainer.train(60, 50)
  
  
}