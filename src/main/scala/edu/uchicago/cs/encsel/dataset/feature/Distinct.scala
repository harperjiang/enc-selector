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

  val primes = Array(198494447, 198494449, 198494477, 198494497, 198494503, 198494507, 198494509, 198494537,
    982447957, 982448011, 982448017, 982448023, 982448069, 982448081, 982448083, 982448099,
    573262177, 573262181, 573262187, 573262213, 573262229, 573262241, 573262247, 573262267,
    982448263, 982448267, 982448279, 982448321, 982448347, 982448393, 982448413, 982448429,
    817507381, 817507391, 817507393, 817507447, 817507451, 817507463, 817507501, 817507517)

  def count(input: Iterator[String], size: Int): Integer = {
    val bitmap = Math.ceil(size.toDouble / 8).toInt
    val bitmapset = input.map(line => {
      val hash = line.hashCode
      primes.map(hash % _).mkString(",").hashCode % bitmap
    }).toSet
    -bitmap * Math.log(1 - bitmapset.size.toDouble / bitmap).toInt
  }
}