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

object Sparsity extends FeatureExtractor {

  def featureType = "Sparsity"

  def supportFilter: Boolean = true

  def extract(input: Column, prefix: String): Iterable[Feature] = {
    var counter = 0
    var emptyCount = 0
    val source = Source.fromFile(input.colFile)
    try {
      source.getLines().foreach(line => {
        counter += 1
        if (line.trim().isEmpty) {
          emptyCount += 1
        }
      })
      val fType = "%s%s".format(prefix, featureType)
      if (counter != 0) {
        Iterable(new Feature(fType, "count", counter),
          new Feature(fType, "empty_count", emptyCount),
          new Feature(fType, "valid_ratio", (counter.toDouble - emptyCount) / counter))
      } else {
        Iterable(new Feature(fType, "count", counter),
          new Feature(fType, "empty_count", emptyCount),
          new Feature(fType, "valid_ratio", 0))
      }
    } finally {
      source.close()
    }
  }
}