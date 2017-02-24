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

class LSTMDataset(file: String) extends Dataset {

  val dict = new HashMap[Char, Int]
  dict += (('{', 0))
  Source.fromFile(file).getLines().foreach(line => {
    line.trim.toCharArray().foreach { c => dict.getOrElseUpdate(c, dict.size) }
  })
  dict += (('}', dict.size))
  dict += (('0', dict.size))
  // Second pass, replace characters with index
  
  val lines = Source.fromFile(file).getLines()
    .map(line => {
      val replaced = line.trim.toCharArray().map { dict.getOrElse(_, 0) }
      (0 +: replaced) :+ (dict.getOrElse('}', -1))
    })
    .toList.sortBy(-_.length)

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
        .padTo(maxlength, dict.getOrElse('0', -1)).map(_.toDouble))).toList
      val data = Nd4j.create(tondarray, Array(slice.size, maxlength))

      new LSTMBatch(slice.size, maxlength, data)
    }).toIterator
  }

  def numChars: Int = dict.size
}

class LSTMBatch(s: Int, len: Int, d: INDArray) extends Batch(s, d, null) {
  def length: Int = len
}