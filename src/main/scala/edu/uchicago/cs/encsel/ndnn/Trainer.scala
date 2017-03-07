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
import scala.util.control.Breaks._

trait Evaluator {
  def init:Unit
  def report[DATA,GT](batch:Batch[DATA,GT], loss:Double, acc:Int):Unit
  def summary:String
}

class MeanLossEvaluator extends Evaluator {
  var batchCounter = 0
  var lossSum = 0d
  var accSum = 0

  def init:Unit = {
    batchCounter = 0
    lossSum = 0
    accSum = 0
  }

  def report[DATA,GT](batch:Batch[DATA,GT], loss:Double, acc:Int) = {
    batchCounter += 1
    lossSum += loss
    accSum += acc
  }

  def summary:String =
    """Average loss %f, average accuracy %f""".format(lossSum/batchCounter, accSum/batchCounter)
}


trait Trainer[DATA, GT, T <: Dataset[DATA, GT], G <: Graph[GT]] {
  val logger = LoggerFactory.getLogger(getClass)

  def getTrainSet: T
  def getTestSet: T
  def getTrainedGraph: G

  protected def getEvaluator: Evaluator
  protected def getGraph(batch: Batch[DATA, GT]): G
  protected def earlyStop = false

  def train(epoches: Int, profiling: Boolean, trainBatchSize: Int = 50, testBatchSize: Int = 50): Unit = {
    val trainset = getTrainSet

    // Initial test
    evaluate(testBatchSize)

    breakable {
      for (i <- 1 to epoches) { // Epoches
        logger.info("[Epoch %d]".format(i))
        val startTime = profiling match { case false => 0 case _ => System.currentTimeMillis() }

        trainset.newEpoch()
        var graph: Graph[GT] = null
        trainset.batches(trainBatchSize).foreach { batch =>
          {
            graph = getGraph(batch)
            graph.train
          }
        }
        if (null != graph)
          graph.epochDone

        evaluate(testBatchSize)
        val es = earlyStop

        val stopTime = System.currentTimeMillis()
        logger.info("Training time %f mins".format((stopTime - startTime) / 60000d))

        if (es) {
          break
        }
      }
    }
  }

  protected def evaluate(testBatchSize: Int = 100): Unit = {
    val testset = getTestSet
    val evaluator = getEvaluator
    evaluator.init
    testset.batches(testBatchSize).foreach {
      batch =>
        {
          val graph = getGraph(batch)
          val (loss, acc) = graph.test
          evaluator.report(batch, loss,acc)
        }
    }
    logger.info("Test Result: %s".format(evaluator.summary))
  }
}

class SimpleTrainer[DATA, GT, T <: Dataset[DATA, GT], G <: Graph[GT]](trainset: T, testset: T, graph: G)
    extends Trainer[DATA, GT, T, G] {

  protected val evaluator = new MeanLossEvaluator()

  def getTrainSet: T = trainset
  def getTestSet: T = testset
  def getTrainedGraph: G = graph
  override protected def getEvaluator = evaluator

  protected def getGraph(batch: Batch[DATA, GT]): G = {
    setInput(batch, graph)
    graph
  }

  protected def setInput(batch: Batch[DATA, GT], graph: G): Unit = Unit
}
