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
package edu.uchicago.cs.encsel.ndnn.example.lstm

import edu.uchicago.cs.encsel.ndnn.TrainerBase
import edu.uchicago.cs.encsel.ndnn.Batch
import scala.collection.mutable.ArrayBuffer
import org.nd4j.linalg.api.ndarray.INDArray
import edu.uchicago.cs.encsel.ndnn.Index
import org.nd4j.linalg.indexing.NDArrayIndex
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMBatch
import org.nd4j.linalg.factory.Nd4j
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph

class LSTMTrainer(dataset: LSTMDataset, layer: Int, hiddenDim: Int)
    extends TrainerBase[LSTMDataset, LSTMGraph](dataset, dataset) {

  protected val params = new ArrayBuffer[INDArray]
  protected var currentGraph: LSTMGraph = null

  def getGraph(batch: Batch): LSTMGraph = {
    val lstmbatch = batch.asInstanceOf[LSTMBatch]
    val textarray = lstmbatch.data
    val numChar = dataset.numChars

    if (currentGraph == null || currentGraph.length != lstmbatch.length) {
      val graph = new LSTMGraph(numChar, hiddenDim, lstmbatch.length)
      if (currentGraph != null) {
        params.clear()
        params ++= currentGraph.dump()
      }
      graph.load(params)
      currentGraph = graph
    }

    // Set X
    currentGraph.xs.zipWithIndex.foreach(pair => {
      val input = pair._1
      val idx = pair._2
      val slice = textarray.get(Index.point(3, 0, idx): _*)
      input.setValue(slice)
    })
    // Set h and c
    val emptyInit = Nd4j.zeros(batch.size, hiddenDim)
    currentGraph.h0.setValue(emptyInit)
    currentGraph.c0.setValue(emptyInit)
    // Set Expect
    currentGraph.expect(lstmbatch.groundTruth)
    currentGraph
  }

  val prefix = "the agreement shows"
  val predictLength = 300

  val testGraph = new LSTMGraph(dataset.numChars, hiddenDim, predictLength, prefix.length())

  // Override the test function to do a prediction of text
  override def test: Unit = {
    if (currentGraph != null)
      testGraph.load(currentGraph.dump())

    val inputs = dataset.translate(prefix)
    // Set xs
    val slice = inputs.map(i => Nd4j.create(Array(i), Array(1, 1)))

    testGraph.xs.zip(slice).foreach(p => {
      p._1.setValue(p._2)
    })

    // set h and c
    val emptyInit = Nd4j.zeros(1, hiddenDim)
    testGraph.h0.setValue(emptyInit)
    testGraph.c0.setValue(emptyInit)
    // Set Expect just to make sure loss function does not complain
    testGraph.expect(Nd4j.zeros(predictLength, 1, 1))
    testGraph.test

    logger.info(testGraph.xs.map(x => dataset.translate(x.value.getFloat(0, 0))).mkString(""))
  }
}