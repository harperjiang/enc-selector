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

package edu.uchicago.cs.encsel.feature

import edu.uchicago.cs.encsel.column.Column
import scala.io.Source

object Sparsity extends FeatureExtractor {
  
  def extract(input: Column): Iterable[Feature] = {
    var counter = 0
    var emptyCount = 0
    Source.fromFile(input.colFile).getLines().foreach {
      line =>
        {
          counter += 1
          if (line.trim().isEmpty()) {
            emptyCount += 1
          }
        }
    }
    Iterable(new Feature("Sparsity", "count", counter),
      new Feature("Sparsity", "empty_count", emptyCount),
      new Feature("Sparsity", "valid_ratio", (counter.toDouble - emptyCount) / counter))
  }
}