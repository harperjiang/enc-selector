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

trait Dataset {
  def size: Int
  def batchSize(size: Int): Unit
  def batchSize(): Int
  def newEpoch(): Unit
  def batches: Iterator[Batch]
}

class Batch(s: Int, d: INDArray, gt: INDArray) {
  val size = s
  val data = d
  val groundTruth = gt
}

abstract class DefaultDataset(ds: Array[Int], gts: Array[Int]) extends Dataset {

  protected val dataShape = ds
  protected val gtShape = gts

  protected var dataSize = -1
  protected var datas: Array[INDArray] = null
  protected var groundTruths: Array[INDArray] = null

  protected var bSize = -1
  protected var permuteIdx: Buffer[Int] = null

  init

  protected def load(): (Int, Array[INDArray], Array[INDArray])

  protected def init = {
    val loaded = load()
    dataSize = loaded._1
    datas = loaded._2
    groundTruths = loaded._3

    permuteIdx = (0 to dataSize - 1).toBuffer
    Collections.shuffle(permuteIdx)
  }

  def size: Int = dataSize

  def batchSize(size: Int) = bSize = size
  def batchSize() = bSize

  def newEpoch() = {
    // Shuffle data, generate random batch
    Collections.shuffle(permuteIdx)
  }

  def batches: Iterator[Batch] = {
    bSize = bSize match { case Dataset.BATCH_ALL => dataSize case _ => bSize }
    if (0 == bSize)
      throw new IllegalArgumentException("Batch Size is ZERO")
    val numBatch = (dataSize / bSize) + ((dataSize % bSize) match { case 0 => 0 case _ => 1 })
    val curBatchSize = bSize
    (0 until numBatch).toIterator.map { i =>
      {
        val idices = permuteIdx.slice(i * curBatchSize, Math.min((i + 1) * curBatchSize, size))
        val dataArray = Nd4j.createUninitialized(Array(idices.size, dataShape: _*))
        val gtArray = Nd4j.createUninitialized(Array(idices.size, gtShape: _*))
        idices.zipWithIndex.foreach(idx => {
          dataArray.put(Array(NDArrayIndex.point(idx._2), NDArrayIndex.all()), datas(idx._1))
          gtArray.put(Array(NDArrayIndex.point(idx._2), NDArrayIndex.all()), groundTruths(idx._1))
        })
        new Batch(idices.size, dataArray, gtArray)
      }
    }
  }
}
