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

package edu.uchicago.cs.encsel.util

import org.apache.commons.lang.StringUtils

object WordUtils {

  def levDistance(a: String, b: String): Double = {
    if (StringUtils.isEmpty(a) || StringUtils.isEmpty(b))
      return Math.max(a.length, b.length)
    var asub = a.substring(0, a.length() - 1)
    var bsub = b.substring(0, b.length() - 1)
    return Array(levDistance(asub, b) + 1,
      levDistance(a, bsub) + 1,
      levDistance(asub, bsub) + (a.last != b.last match { case true => 1 case _ => 0 })).min
  }
  /**
   * Add a weight to lev distance, a difference at position p has a difference 1.15 - tanh (0.15p)
   * This has an effort that the first char has distance 1, the 5th has distance 0.5 and chars after 10 has 0.15
   */
  def levDistance2(a: String, b: String): Double = {
    if (StringUtils.isEmpty(a) || StringUtils.isEmpty(b))
      return (1 to Math.max(a.length, b.length).intValue()).map(weight(_)).sum
    var asub = a.substring(0, a.length() - 1)
    var bsub = b.substring(0, b.length() - 1)
    return Array(levDistance(asub, b) + weight(a.length),
      levDistance(a, bsub) + weight(b.length),
      levDistance(asub, bsub) + (a.last != b.last match { case true => weight(Math.max(a.length, b.length)) case _ => 0 })).min
  }

  protected def weight(idx: Int): Double = 1.15 - Math.tanh(0.15 * idx)

}