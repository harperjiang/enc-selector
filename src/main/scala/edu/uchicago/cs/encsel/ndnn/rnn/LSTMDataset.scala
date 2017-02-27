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
package edu.uchicago.cs.encsel.ndnn.rnn

import edu.uchicago.cs.encsel.ndnn.Dataset
import org.nd4j.linalg.api.ndarray.INDArray
import edu.uchicago.cs.encsel.ndnn.Dataset
import edu.uchicago.cs.encsel.ndnn.Batch
import scala.io.Source
import scala.collection.mutable.HashMap
import org.nd4j.linalg.factory.Nd4j
import scala.collection.JavaConversions._
import java.io.InputStream
import edu.uchicago.cs.encsel.ndnn.Index
import org.nd4j.linalg.indexing.NDArrayIndex

class LSTMDataset(file: String) extends Dataset {

  private val dict = new HashMap[Char, Int]

  dict += (('{', 0))
  Source.fromFile(file).getLines().foreach(line => {
    line.trim.toCharArray().foreach { c => dict.getOrElseUpdate(c, dict.size) }
  })
  dict += (('}', dict.size))
  dict += (('\0', dict.size))

  private val inverseDict = dict.map(p => (p._2, p._1))
  // Second pass, replace characters with index

  protected val lines = Source.fromFile(file).getLines()
    .map(line => {
      val replaced = line.trim.toCharArray().map { dict.getOrElse(_, 0) }
      (0 +: replaced) :+ (dict.getOrElse('}', -1))
    })
    .toList.sortBy(-_.length)
  this.dataSize = lines.length

  def newEpoch(): Unit = {

  }

  def batches: Iterator[Batch] = {
    val numBatches = lines.length / batchSize + (
      (lines.length % batchSize) match { case 0 => 0 case _ => 1 })
    (for (i <- 0 until numBatches) yield {
      val from = i * batchSize
      val to = Math.min(lines.length, (i + 1) * batchSize)
      val slice = lines.slice(from, to)
      val maxlength = slice.map(_.length).max

      val tondarray = slice.map(line => Nd4j.create(line
        .padTo(maxlength, dict.getOrElse('\0', -1)).map(_.toDouble))).toList

      val indices = Array(NDArrayIndex.all(), NDArrayIndex.all(),
        NDArrayIndex.newAxis())
      // Shape L,B,1
      val data = Nd4j.create(tondarray, Array(slice.size, maxlength))
        .transposei.get(indices: _*)
      // Both have Shape (L-1),B,1
      val droplast = data.get(Index.range(3, 0, (0, maxlength - 1)): _*)
      val dropfirst = data.get(Index.range(3, 0, (1, maxlength)): _*)

      new LSTMBatch(slice.size, maxlength - 1, droplast, dropfirst)
    }).toIterator
  }

  def translate(input: String): Array[Double] =
    input.toCharArray().map(dict.getOrElse(_, -1).toDouble)

  def translate(input: Double): Char = inverseDict.getOrElse(input.toInt,
    throw new IllegalArgumentException(input.toString()))

  def numChars: Int = dict.size
}

class LSTMBatch(s: Int, len: Int, d: INDArray, gt: INDArray) extends Batch(s, d, gt) {
  def length: Int = len

}