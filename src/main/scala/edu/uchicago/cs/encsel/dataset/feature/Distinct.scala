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
    val size = Source.fromFile(new File(col.colFile)).getLines().toSet.size
    Array(new Feature(featureType, "distinct", size),
      new Feature(featureType, "distinct_ratio", size.toDouble / sum))
  }
}