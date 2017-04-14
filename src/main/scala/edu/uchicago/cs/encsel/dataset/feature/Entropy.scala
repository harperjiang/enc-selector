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
import edu.uchicago.cs.encsel.util.DataUtils
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory

object Entropy extends FeatureExtractor {

  var logger = LoggerFactory.getLogger(getClass)

  def featureType = "Entropy"

  def extract(input: Column): Iterable[Feature] = {
    val allcalc = new EntropyCalc()
    val linecalc = new EntropyCalc()

    val lineEntropy = Source.fromFile(new File(input.colFile)).getLines()
      .filter(StringUtils.isNotEmpty)
      .map(line => {
        allcalc.add(line);
        entropy(line, linecalc)
      }).toTraversable
    if (0 == lineEntropy.size)
      return Iterable[Feature]()
    val stat = DataUtils.stat(lineEntropy)
    Iterable(new Feature(featureType, "line_max", lineEntropy.max),
      new Feature(featureType, "line_min", lineEntropy.min),
      new Feature(featureType, "line_mean", stat._1),
      new Feature(featureType, "line_var", stat._2),
      new Feature(featureType, "total", allcalc.done()))
  }

  def entropy(data: String, linecalc: EntropyCalc): Double = {
    linecalc.reset()
    linecalc.add(data)
    linecalc.done()
  }
}

class EntropyCalc {

  var counter = scala.collection.mutable.HashMap[Char, Double]()

  def add(data: String): Unit = {
    data.toCharArray.foreach(c => {
      counter += ((c, counter.getOrElse(c, 0d) + 1))
    })
  }

  def reset(): Unit = {
    counter.clear()
  }

  def done(): Double = {
    val sum = counter.values.sum
    counter.map(entry => {
      val p = (entry._2 / sum);
      -p * Math.log(p)
    }).sum
  }
}