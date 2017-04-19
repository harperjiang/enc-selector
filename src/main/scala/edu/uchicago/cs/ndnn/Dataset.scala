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
package edu.uchicago.cs.ndnn

import java.util.Collections

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

import scala.collection.JavaConversions._
import scala.collection.mutable

trait Dataset[D] {

  def batches(batchSize: Int): Iterator[Batch[D]]

  def numBatch: Int

  def dataSize: Int
}

class Batch[D](val size: Int, val data: D, val groundTruth: D)

abstract class DatasetBase[D] extends Dataset[D] {

  protected var datas: Array[Array[Double]] = _
  protected var expects: Array[Double] = _
  protected var permuteIdx: mutable.Buffer[Int] = _
  var numBatch = 0

  {
    val loaded = load()
    datas = loaded._1
    expects = loaded._2
    permuteIdx = (0 until datas.length).toBuffer
  }

  def dataSize = datas.length

  protected def load(): (Array[Array[Double]], Array[Double])

  protected def construct(idices: Seq[Int]): Batch[D]

  def batches(batchSize: Int): Iterator[Batch[D]] = {
    Collections.shuffle(permuteIdx)

    if (batchSize <= 0)
      throw new IllegalArgumentException("Batch Size should be non-negative")
    val batches = (0 until datas.length by batchSize)
    numBatch = batches.length

    batches.toIterator.map(i => {
      val idices = permuteIdx.slice(i, i + batchSize)
      construct(idices)
    })
  }
}

abstract class DefaultDataset extends DatasetBase[INDArray] {

  protected def construct(idices: Seq[Int]): Batch[INDArray] = {
    new Batch[INDArray](idices.length, Nd4j.create(idices.map(datas(_)).toArray),
      Nd4j.create(idices.map(expects(_)).toArray))
  }
}

abstract class VarLenDatasetBase[D] extends Dataset[D] {

  protected var datas: Array[Array[Double]] = _
  protected var expects: Array[Double] = _
  protected var groupByLength: Array[Array[Int]] = _
  protected var permuteIdx: mutable.Buffer[Int] = _

  var numBatch = 0

  {
    val loaded = load()
    datas = loaded._1
    expects = loaded._2
    groupByLength = loaded._1.zipWithIndex.map(d => (d._1.length, d._2)).groupBy(_._1)
      .values.map(_.map(_._2)).toArray
    permuteIdx = (0 until groupByLength.length).toBuffer
  }

  def dataSize = datas.length

  protected def load(): (Array[Array[Double]], Array[Double])

  protected def construct(idices: Seq[Int]): Batch[D]

  def batches(batchSize: Int): Iterator[Batch[D]] = {
    Collections.shuffle(permuteIdx)

    if (batchSize <= 0)
      throw new IllegalArgumentException("Batch Size should be non-negative")

    numBatch = groupByLength.map(group => (0 until group.length by batchSize).length).sum

    permuteIdx.map(idx => {
      val group = groupByLength(idx)
      (0 until group.length by batchSize).map(iidx => {
        construct(group.slice(iidx, iidx + batchSize))
      })
    }).flatten.toIterator
  }
}

abstract class DefaultVarLenDataset extends VarLenDatasetBase[INDArray] {

  protected def construct(idices: Seq[Int]): Batch[INDArray] = {
    new Batch[INDArray](idices.length, Nd4j.create(idices.map(datas(_)).toArray),
      Nd4j.create(idices.map(expects(_)).toArray))
  }
}
