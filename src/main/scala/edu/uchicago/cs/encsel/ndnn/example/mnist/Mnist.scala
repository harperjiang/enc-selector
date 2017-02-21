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

object Mnist extends App {
  val folder = "/home/harper"

  val trainDataFile = folder + "/dataset/mnist/train-images.idx3-ubyte"
  val trainLabelFile = folder + "/dataset/mnist/train-labels.idx1-ubyte"
  val testDataFile = folder + "/dataset/mnist/t10k-images.idx3-ubyte"
  val testLabelFile = folder + "/dataset/mnist/t10k-labels.idx1-ubyte"

  val trainset = new MnistDataset(trainDataFile, trainLabelFile )
  val testset = new MnistDataset(testDataFile, testLabelFile)

  val graph = new MnistGraph()

  testset.batchSize(Dataset.BATCH_ALL)
  val testbatch = testset.batches.next()
  graph.pixelInput.setValue(testbatch.data)
  graph.expect(testbatch.groundTruth)
  val (loss, acc) = graph.test

  println("Initial Accuracy: %f".format(acc.doubleValue() / testbatch.size))

  trainset.batchSize(50)

  for (i <- 1 to 30) { // Epoches
    trainset.newEpoch()
    trainset.batches.foreach { batch =>
      {
        graph.pixelInput.setValue(batch.data)
        graph.expect(batch.groundTruth)
        graph.train
      }
    }
    testset.batchSize(Dataset.BATCH_ALL)
    val testbatch = testset.batches.next()
    graph.pixelInput.setValue(testbatch.data)
    graph.expect(testbatch.groundTruth)
    val (loss, acc) = graph.test
    println("Epoch %d, accuracy %d %f".format(i, acc, acc.doubleValue() / testbatch.size))
  }

  testset.batchSize(Dataset.BATCH_ALL)
  val testbatch2 = testset.batches.next()
  graph.pixelInput.setValue(testbatch2.data)
  graph.expect(testbatch2.groundTruth)
  val (loss2, acc2) = graph.test
  println("Final Accuracy: %f".format(acc2.doubleValue() / testbatch2.size))

}