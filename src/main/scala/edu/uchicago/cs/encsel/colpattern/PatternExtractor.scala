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

package edu.uchicago.cs.encsel.colpattern

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

object PatternExtractor {
  val threshold = 0.1
}

class PatternExtractor {

  private val lexer = Lexer

  private val wordFrequency = new HashMap[String, Double]
  private val sections = new ArrayBuffer[Section]

  def extract(lines: Seq[String]): Pattern = {

    val tokens = lines.map(lexer.tokenize(_)
      .map(_.content).toIndexedSeq)

    // Compute word frequency
    wordFrequency.clear()
    val tokenGroup = tokens.map(_.toSet.toList)
      .flatten.groupBy(k => k).mapValues(_.length)
    val lineCount = lines.length
    wordFrequency ++= tokenGroup.mapValues(_.toDouble / lineCount)

    // Look for hot spots in lines
    val hspots = tokens.map(hotspots(_))
    null
  }
  /**
   * Look for hot spots in each line. Hot spots are words that frequently occurs
   * in text. The returned type is (text, frequency)
   */
  protected def hotspots(line: IndexedSeq[String]): Array[(String, Double)] = {
    line.map(word => (word, wordFrequency.getOrElse(word, 0d)))
      .filter(_._2 >= PatternExtractor.threshold).toArray
  }
}