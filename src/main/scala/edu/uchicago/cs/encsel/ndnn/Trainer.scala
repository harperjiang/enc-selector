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

trait Trainer[T <: Dataset, G <: Graph] {
  val logger = LoggerFactory.getLogger(getClass)

  def getTrainset: T
  def getTestset: T
  def getGraph(batch: Batch): G

  def train(epoches: Int, profiling: Boolean): Unit = {
    logger.info("[Initial Result]")
    test

    val trainset = getTrainset
    val testset = getTestset

    trainset.batchSize(50)

    for (i <- 1 to epoches) { // Epoches
      logger.info("[Epoch %d]".format(i))
      val startTime = profiling match { case false => 0 case _ => System.currentTimeMillis() }

      trainset.newEpoch()
      trainset.batches.foreach { batch =>
        {
          val graph = getGraph(batch)
          graph.expect(batch.groundTruth)
          graph.train
        }
      }
      if (profiling) {
        val stopTime = System.currentTimeMillis()
        logger.info("Training time %f mins".format((stopTime - startTime) / 60000d))
      }
      test
    }
  }

  def test: Unit = {
    val testset = getTestset
    testset.batchSize(Dataset.BATCH_ALL)
    val testbatch = testset.batches.next()
    val graph = getGraph(testbatch)
    graph.expect(testbatch.groundTruth)
    val (loss, acc) = graph.test

    logger.info("Accuracy: %f".format(acc.doubleValue() / testbatch.size))
  }
}

abstract class TrainerBase[T <: Dataset, G <: Graph](trainset: T, testset: T) extends Trainer[T, G] {
  def getTrainset: T = trainset
  def getTestset: T = testset

}

class SimpleTrainer[T <: Dataset, G <: Graph](trainset: T, testset: T, grh: G) extends TrainerBase[T, G](trainset, testset) {

  def getGraph(batch: Batch): G = {
    setInput(batch, grh)
    grh
  }

  protected def setInput(batch: Batch, graph: G): Unit = Unit
}
