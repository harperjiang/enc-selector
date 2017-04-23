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
import scala.collection.mutable.ArrayBuffer

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
    permuteIdx = datas.indices.toBuffer
  }

  def dataSize = datas.length

  protected def load(): (Array[Array[Double]], Array[Double])

  protected def construct(idices: Seq[Int]): Batch[D]

  def batches(batchSize: Int): Iterator[Batch[D]] = {
    Collections.shuffle(permuteIdx)

    if (batchSize <= 0)
      throw new IllegalArgumentException("Batch Size should be non-negative")
    val batches = (datas.indices by batchSize)
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
      Nd4j.create(idices.map(expects(_)).toArray).reshape(idices.length, -1))
  }

  def split(ratio: Seq[Double]): Seq[Dataset[INDArray]] = {

    val length = this.datas.length
    val permutation = (0 until length).toBuffer
    Collections.shuffle(permutation)

    val slice = ratio.map(d => Math.floor(d * length).toInt).toBuffer
    val addup = length - slice.sum

    slice.last += addup

    var pointer = 0

    slice.map(cnt => {
      val result = fetchSub(permutation, pointer, pointer + cnt)
      pointer += cnt
      result
    })
  }

  protected def fetchSub(permutation: Seq[Int], from: Int, to: Int): Dataset[INDArray] = {
    val data = new ArrayBuffer[Array[Double]]
    val label = new ArrayBuffer[Double]
    (from until to).foreach(idx => {
      data += this.datas(permutation(idx))
      label += this.expects(permutation(idx))
    })
    new PreparedDataset(data.toArray, label.toArray)
  }
}

class PreparedDataset(val features: Array[Array[Double]],
                      val expect: Array[Double])
  extends DefaultDataset {

  override def load(): (Array[Array[Double]], Array[Double]) = (features, expect)
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
    permuteIdx = groupByLength.indices.toBuffer
  }

  def dataSize = datas.length

  protected def load(): (Array[Array[Double]], Array[Double])

  protected def construct(idices: Seq[Int]): Batch[D]

  def batches(batchSize: Int): Iterator[Batch[D]] = {
    Collections.shuffle(permuteIdx)

    if (batchSize <= 0)
      throw new IllegalArgumentException("Batch Size should be non-negative")

    numBatch = groupByLength.map(group => (group.indices by batchSize).length).sum

    permuteIdx.flatMap(idx => {
      val group = groupByLength(idx)
      (0 until group.length by batchSize).map(iidx => {
        construct(group.slice(iidx, iidx + batchSize))
      })
    }).toIterator
  }
}

abstract class DefaultVarLenDataset extends VarLenDatasetBase[INDArray] {

  protected def construct(idices: Seq[Int]): Batch[INDArray] = {
    new Batch[INDArray](idices.length, Nd4j.create(idices.map(datas(_)).toArray),
      Nd4j.create(idices.map(expects(_)).toArray))
  }
}
