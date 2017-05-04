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

import java.util.Comparator

import edu.uchicago.cs.encsel.dataset.column.Column

import scala.io.Source

/**
  * This feature computes how much the dataset is sorted by compute the number
  * of inverted pairs compared to
  */
class Sortness(val windowSize: Int = 2) extends FeatureExtractor {

  def featureType: String = "Sortness"

  def supportFilter: Boolean = true

  def extract(input: Column,
              filter: (Iterator[String]) => Iterator[String],
              prefix: String): Iterable[Feature] = {

    val source = Source.fromFile(input.colFile)
    try {
      var sum = 0
      var inverted = 0
      if (windowSize != -1) {
        filter(source.getLines()).sliding(windowSize, 1).foreach(group => {
          val (invert, total) = computeInvertPair(group, input.dataType.comparator())
          sum += total
          inverted += invert
        })
      } else {
        val (invert, total) = computeInvertPair(filter(source.getLines()).toSeq,
          input.dataType.comparator())
        sum += total
        inverted += invert
      }
      val fType = featureType(prefix)
      if (0 != sum) {
        // 1 - abs(2x-1)
        val ratio = (sum - inverted).toDouble / sum
        val measurement = 1 - Math.abs(2 * ratio - 1)
        Iterable(
          new Feature(fType, "%d_totalpair".format(windowSize), sum),
          new Feature(fType, "%d_ivpair".format(windowSize), measurement)
        )
      } else {
        Iterable(
          new Feature(fType, "%d_totalpair".format(windowSize), sum),
          new Feature(fType, "%d_ivpair".format(windowSize), 0)
        )
      }
    } finally {
      source.close()
    }
  }

  protected def computeInvertPair(input: Seq[String],
                                  comparator: Comparator[String]): (Int, Int) = {
    if (input.isEmpty)
      return (0, 0)
    var invert = 0
    input.indices.foreach(i => {
      input.indices.drop(i + 1).foreach(j => {
        if (comparator.compare(input(i), input(j)) > 0) {
          invert += 1
        }
      })
    })
    return (invert, input.length * (input.length + 1) / 2)
  }
}
