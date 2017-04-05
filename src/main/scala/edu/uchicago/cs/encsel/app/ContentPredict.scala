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

package edu.uchicago.cs.encsel.app

import edu.uchicago.cs.encsel.dataset.persist.Persistence
import edu.uchicago.cs.encsel.dataset.persist.jpa.JPAPersistence
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph
import org.nd4j.linalg.factory.Nd4j
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMPredictGraph
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMTrainer
import edu.uchicago.cs.encsel.ndnn.SGD

object ContentPredict extends App {

  //  val jpa = Persistence.get.asInstanceOf[JPAPersistence]
  //  val column = jpa.find(args(0).toInt)

  val batchSize = 50
  val hiddenDim = 100
  val trainset = new LSTMDataset("/home/harper/enc_workspace/train.txt")
  val testset = new LSTMDataset("/home/harper/enc_workspace/test.txt")
  val traingraph = new LSTMGraph(trainset.numChars, hiddenDim, new SGD(0.5, 1))
  val predict = new LSTMPredictGraph(trainset.numChars, hiddenDim)

  val trainer = new LSTMTrainer(trainset, testset, traingraph) {
    override def evaluate(testBatchSize: Int) = {
      super.evaluate(testBatchSize)
      predict.load(traingraph.dump())
      val prefix = 1
      predict.build(20, prefix)
      predict.xs(0).set(Array(getTrainSet.translate("{")(0)))
      val sz = Nd4j.zeros(1, hiddenDim)
      predict.h0.set(sz)
      predict.c0.set(sz)
      predict.test
      println(predict.predicts.map(p => getTrainSet.translate(p.getValue.getDouble(0, 0))).mkString(""))
    }
  }

  trainer.train(50, 50)
}
