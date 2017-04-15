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
package edu.uchicago.cs.encsel.dataset.feature

import edu.uchicago.cs.encsel.dataset.column.Column
import scala.io.Source
import java.io.File

object Distinct extends FeatureExtractor {
  def featureType = "Distinct"

  def extract(col: Column): Iterable[Feature] = {
    val sum = Source.fromFile(new File(col.colFile)).getLines().count(_ => true)
    val size = DistinctCounter.count(Source.fromFile(new File(col.colFile)).getLines(), sum)
    Array(new Feature(featureType, "count", size.toDouble),
      new Feature(featureType, "ratio", size.toDouble / sum))
  }
}

object DistinctCounter {

  def count(input: Iterator[String], size: Int): Int = {
    val bitmapSize = Math.ceil(size.toDouble / 64).toInt
    val bitsize = bitmapSize * 64
    val bitmap = new Array[Long](bitmapSize)
    input.foreach(line => {
      val hash = Math.abs(line.hashCode) % bitsize
      bitmap(hash / 64) |= 1 << (hash % 64)
    })
    val occupy = bitmap.map(java.lang.Long.bitCount(_)).sum.toDouble
    val empty = 1d - occupy/bitsize
    (-bitsize * Math.log(empty)).toInt
  }
}