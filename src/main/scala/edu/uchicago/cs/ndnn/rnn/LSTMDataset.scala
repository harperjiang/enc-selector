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

import scala.collection.mutable.{ArrayBuffer, Buffer, HashMap}
import scala.io.Source

trait LSTMTokenizer {
  def tokenize(input: String): Iterable[String]
}

class WordTokenizer extends LSTMTokenizer {

  override def tokenize(input: String): Iterable[String] = {
    return input.split("\\s+")
  }
}

class CharTokenizer extends LSTMTokenizer {

  override def tokenize(input: String): Iterable[String] = {
    return input.toCharArray.map(_.toString)
  }
}

class LSTMData(data: Array[Double]) extends Data {
  def length(): String = data.length.toString

  def numPiece(): Int = 1

  def feature(): Array[Array[Double]] = Array(data)

  def groundTruth(): Double = 0d
}

class S2SData(from: Array[Double], to: Array[Double]) extends Data {

  def length(): String = (from.length, to.length).toString()

  def numPiece(): Int = 2

  def feature(): Array[Array[Double]] = Array(from, to)

  def groundTruth(): Double = 0d
}


class LSTMDataset(file: String, tokenizer: LSTMTokenizer, extdict: LSTMDataset = null) extends VarLenDataset {

  private var dict: HashMap[String, Int] = _
  private var inverseDict: Buffer[String] = _

  def this(file: String) {
    this(file, new WordTokenizer, null)
  }

  override protected def load(): Array[Data] = {
    dict = new HashMap[String, Int]
    inverseDict = new ArrayBuffer[String]
    val source = Source.fromFile(file)
    try {
      val content = source.getLines().map(line => tokenizer.tokenize(line)).toArray
      if (extdict == null) {
        content.map(line =>
          new LSTMData(line.map(c =>
            dict.getOrElseUpdate(c, {
              inverseDict += c
              dict.size
            }).toDouble
          ).toArray).asInstanceOf[Data])
      } else {
        dict ++= extdict.dict
        inverseDict ++= extdict.inverseDict
        content.map(line => new LSTMData(line.map(dict.getOrElse(_, 0).toDouble).toArray))
      }
    } finally {
      source.close()
    }
  }

  def dictSize: Int = dict.size
}