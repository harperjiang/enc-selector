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
 */

package edu.uchicago.cs.encsel.word

import scala.collection.mutable.HashMap
import org.nd4j.linalg.api.ndarray.INDArray
import scala.io.Source
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.ops.transforms.Transforms

object WordVecDict {
  val bufferSize = 500
}

class WordVecDict(source: String) {

  val buffer = new HashMap[String, Option[INDArray]]

  def find(key: String): Option[INDArray] = {
    buffer.getOrElseUpdate(key, {
      if (buffer.size >= WordVecDict.bufferSize) {
        buffer.drop(buffer.size - WordVecDict.bufferSize + 1)
      }
      load(key)
    })
  }

  /**
   * Compute the cosine similarity between two vectors
   */
  def compare(word1: String, word2: String): Double = {
    val data1 = find(word1)
    val data2 = find(word2)
    if (data1.isEmpty || data2.isEmpty) {
      throw new IllegalArgumentException("Word not found")
    }
    Transforms.cosineSim(data1.get, data2.get)
  }

  protected def load(key: String): Option[INDArray] = {
    val found = Source.fromFile(source).getLines.map(_.split("\\s+"))
      .find { _(0).equals(key) }
    found.isEmpty match {
      case true => None
      case _ => Some(Nd4j.create(found.get.drop(1).map { _.toDouble }))
    }
  }
}