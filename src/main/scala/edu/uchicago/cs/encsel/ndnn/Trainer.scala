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

import edu.uchicago.cs.encsel.ndnn.Dataset
import edu.uchicago.cs.encsel.ndnn.Graph
import edu.uchicago.cs.encsel.ndnn.Batch
import org.slf4j.LoggerFactory

trait Trainer {
  val logger = LoggerFactory.getLogger(getClass)

  def train: Unit
  def test: Unit
}

abstract class SimpleTrainer[T <: Dataset, G <: Graph](trainset: T, testset: T, grh: G, epoches: Int, p: Boolean) extends Trainer {

  val graph = grh
  val profiling = p

  def setInput(batch: Batch, graph: G)

  def train: Unit = {
    test

    trainset.batchSize(50)

    for (i <- 1 to epoches) { // Epoches
      val startTime = profiling match { case false => 0 case _ => System.currentTimeMillis() }

      trainset.newEpoch()
      trainset.batches.foreach { batch =>
        {
          setInput(batch, graph)
          graph.expect(batch.groundTruth)
          graph.train
        }
      }
      if (profiling) {
        val stopTime = System.currentTimeMillis()
        logger.info("Epoch %d, training time %f secs".format(stopTime - startTime))
      }
      testset.batchSize(Dataset.BATCH_ALL)
      val testbatch = testset.batches.next()
      setInput(testbatch, graph)
      graph.expect(testbatch.groundTruth)
      val (loss, acc) = graph.test
      logger.info("Epoch %d, accuracy %d %f".format(i, acc, acc.doubleValue() / testbatch.size))
    }
    test
  }

  def test: Unit = {
    testset.batchSize(Dataset.BATCH_ALL)
    val testbatch = testset.batches.next()
    setInput(testbatch, graph)
    graph.expect(testbatch.groundTruth)
    val (loss, acc) = graph.test

    logger.info("Accuracy: %f".format(acc.doubleValue() / testbatch.size))
  }
}