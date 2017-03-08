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

package edu.uchicago.cs.encsel.dataset.feature

import edu.uchicago.cs.encsel.dataset.column.Column
import scala.io.Source
import java.io.File
import edu.uchicago.cs.encsel.util.DataUtils
import org.apache.commons.lang.StringUtils

object Length extends FeatureExtractor {
  
  def extract(input: Column): Iterable[Feature] = {
    val length = Source.fromFile(new File(input.colFile)).getLines()
      .filter(StringUtils.isNotEmpty(_)).map(_.length().toDouble).toTraversable
    if (0 == length.size)
      return Iterable[Feature]()
    val statforlen = DataUtils.stat(length)

    Iterable(
      new Feature("Length", "max", length.max),
      new Feature("Length", "min", length.min),
      new Feature("Length", "mean", statforlen._1),
      new Feature("Length", "variance", statforlen._2))
  }
}