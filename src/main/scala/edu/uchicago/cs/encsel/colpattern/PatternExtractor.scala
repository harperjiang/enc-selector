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

abstract class PatternExtractor {

  private val lexer = Lexer

  private val wordFrequency = new HashMap[String, Double]
  private val sections = new ArrayBuffer[Section]

  def extract(lines: Seq[String]): Pattern = {

    val tokens = lines.map(lexer.tokenize(_).toIndexedSeq)

    // Compute word frequency
    wordFrequency.clear()

    val tokenGroup = tokens.map(_.map(_.toString).toSet.toList)
      .flatten.groupBy(k => k).mapValues(_.length)
    val lineCount = lines.length
    wordFrequency ++= tokenGroup.mapValues(_.toDouble / lineCount)

    sections.clear
    val numTokens = tokens.map(_.length).max
    sections ++= (0 until numTokens).map(i => new BinarySection())

    while (true) {
      val assignments = tokens.map(line_match(_))
      re_section(assignments)
    }
    new Pattern(sections)
  }
  /**
   * Look for the match with minimal loss for the given line
   */
  protected def line_match(tokens: IndexedSeq[Token]): IndexedSeq[Int] = {
    throw new UnsupportedOperationException()
  }

  protected def re_section(assigns: Seq[IndexedSeq[Int]]): IndexedSeq[Section] = {
    throw new UnsupportedOperationException()
  }
}