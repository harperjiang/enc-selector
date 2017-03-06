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

import org.nd4j.linalg.api.ndarray.INDArray
import java.util.Arrays
import java.util.Collections
import scala.collection.JavaConversions._
import org.nd4j.linalg.factory.Nd4j
import scala.collection.mutable.Buffer
import org.nd4j.linalg.indexing.NDArrayIndex

object Dataset {
  val BATCH_ALL = -1
}

trait Dataset[D, G] {

  protected var dataSize = -1

  def size: Int = dataSize

  def newEpoch(): Unit
  def batches(batchSize: Int): Iterator[Batch[D, G]]
}

class Batch[D, G](s: Int, d: D, gt: G) {
  val size = s
  val data = d
  val groundTruth = gt
}

abstract class DatasetBase[D, G] extends Dataset[D, G] {

  protected var permuteIdx: Buffer[Int] = null

  protected def initShuffle(permute: Boolean = true) = {
    permuteIdx = (0 to dataSize - 1).toBuffer
    if (permute)
      Collections.shuffle(permuteIdx)
  }

  protected def construct(idices: Buffer[Int]): Batch[D, G]

  def newEpoch() = {
    // Shuffle data, generate random batch
    Collections.shuffle(permuteIdx)
  }

  def batches(batchSize: Int): Iterator[Batch[D, G]] = {
    val bSize = batchSize match { case Dataset.BATCH_ALL => dataSize case _ => batchSize }
    if (0 == bSize)
      throw new IllegalArgumentException("Batch Size is ZERO")
    val numBatch = (dataSize / bSize) + ((dataSize % bSize) match { case 0 => 0 case _ => 1 })
    val curBatchSize = bSize
    (0 until numBatch).toIterator.map { i =>
      {
        val idices = permuteIdx.slice(i * curBatchSize, Math.min((i + 1) * curBatchSize, size))
        construct(idices)
      }
    }
  }
}

abstract class DefaultDataset(ds: Array[Int], gts: Array[Int]) extends DatasetBase[INDArray, INDArray] {

  protected val dataShape = ds
  protected val gtShape = gts

  protected var datas: Array[Array[Double]] = null
  protected var groundTruths: Array[Array[Double]] = null

  {
    val loaded = load()
    dataSize = loaded._1
    datas = loaded._2
    groundTruths = loaded._3

    initShuffle(true)
  }

  protected def load(): (Int, Array[Array[Double]], Array[Array[Double]])

  protected def construct(indices: Buffer[Int]): Batch[INDArray, INDArray] = {
    val bSize = indices.length
    val data = Nd4j.create(indices.flatMap { datas(_) }.toArray, Array(bSize, ds: _*))
    val gt = Nd4j.create(indices.flatMap { groundTruths(_) }.toArray, Array(bSize, gts: _*))
    new Batch[INDArray, INDArray](indices.length, data, gt)
  }
}
