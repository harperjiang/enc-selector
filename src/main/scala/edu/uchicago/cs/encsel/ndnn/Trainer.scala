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

import org.slf4j.LoggerFactory

trait Trainer[DATA, GT, T <: Dataset[DATA, GT], G <: Graph[GT]] {
  val logger = LoggerFactory.getLogger(getClass)

  def getTrainset: T
  def getTestset: T
  def getGraph(batch: Batch[DATA, GT]): G

  def train(epoches: Int, profiling: Boolean, trainBatchSize: Int = 100): Unit = {
    val trainset = getTrainset

    trainset.batchSize(trainBatchSize)

    for (i <- 1 to epoches) { // Epoches
      logger.info("[Epoch %d]".format(i))
      val startTime = profiling match { case false => 0 case _ => System.currentTimeMillis() }

      trainset.newEpoch()
      var graph: Graph[GT] = null
      trainset.batches.foreach { batch =>
        {
          graph = getGraph(batch)
          graph.train
        }
      }
      if (null != graph)
        graph.epochDone
      if (profiling) {
        val stopTime = System.currentTimeMillis()
        logger.info("Training time %f mins".format((stopTime - startTime) / 60000d))
      }
    }
  }

  def test(testBatchSize: Int = 100): Unit = {
    val testset = getTestset
    testset.batchSize(testBatchSize)
    var total = 0
    var totalLoss = 0d
    var totalAcc = 0
    var numBatch = 0
    testset.batches.foreach {
      batch =>
        {
          val graph = getGraph(batch)
          val (loss, acc) = graph.test
          total += batch.size
          totalLoss += loss
          totalAcc += acc
          numBatch += 1
        }
    }
    logger.info("Test Result: average loss: %f, accuracy: %f".format(totalLoss / numBatch, totalAcc.doubleValue() / numBatch))
  }
}

class SimpleTrainer[DATA, GT, T <: Dataset[DATA, GT], G <: Graph[GT]](trainset: T, testset: T, graph: G)
    extends Trainer[DATA, GT, T, G] {

  def getTrainset: T = trainset
  def getTestset: T = testset
  def getGraph(batch: Batch[DATA, GT]): G = {
    setInput(batch, graph)
    graph
  }

  protected def setInput(batch: Batch[DATA, GT], graph: G): Unit = Unit
}
