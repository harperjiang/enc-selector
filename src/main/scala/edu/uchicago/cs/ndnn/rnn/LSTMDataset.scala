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
package edu.uchicago.cs.ndnn.rnn

import java.io.File
import java.net.URI

import edu.uchicago.cs.ndnn._
import edu.uchicago.cs.ndnn.{Batch, DatasetBase}

import scala.collection.mutable.{ArrayBuffer, Buffer, HashMap}
import scala.io.Source

object LSTMDataset {
  val PAD = '@'
}

class LSTMDataset(file: URI, extdict: LSTMDataset) extends DatasetBase[Array[Array[Int]]] {

  private var dict: HashMap[Char, Int] = _
  private var inverseDict: Buffer[Char] = _

  def this(file: String)(implicit extdict: LSTMDataset = null) {
    this(new File(file).toURI, extdict)
  }

  def this(file: URI) {
    this(file, null)
  }

  override protected def load(): (Array[Array[Double]], Array[Double]) = {
    dict = new HashMap[Char, Int]
    inverseDict = new ArrayBuffer[Char]
    val strlines = Source.fromFile(file).getLines().map(line =>
      "{%s}".format(line.trim.toLowerCase)).toBuffer
    if (extdict == null) {
      dict += ((LSTMDataset.PAD, 0))
      inverseDict += LSTMDataset.PAD
      strlines.foreach(line =>
        line.toCharArray.foreach { c =>
          dict.getOrElseUpdate(c, {
            inverseDict += c;
            dict.size
          })
        })
    } else {
      dict ++= extdict.dict
      inverseDict ++= extdict.inverseDict
    }

    // Second pass, replace characters with index
    val lines = strlines.map(line =>
      line.toCharArray.map(dict.getOrElse(_, 0).toDouble)).toArray
    strlines.clear
    (lines, Array.empty[Double])
  }

  protected def construct(idices: Seq[Int]): Batch[Array[Array[Int]]] = {
    val data = idices.map(datas(_).map(_.toInt))
    val maxlength = data.map(_.length).max
    val padded = data.map(_.padTo(maxlength, dict.getOrElse(LSTMDataset.PAD, -1)))
    val transposed = (0 until maxlength).map(i => padded.map(_ (i)).toArray).toArray

    new Batch[Array[Array[Int]]](idices.length, transposed.dropRight(1), transposed.drop(1))
  }

  def translate(input: String): Array[Int] =
    input.toCharArray.map(dict.getOrElse(_, -1))

  def translate(input: Double): Char = inverseDict(input.toInt)

  def numChars: Int = dict.size
}