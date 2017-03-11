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

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.ops.transforms.Transforms

import edu.uchicago.cs.encsel.word.WordVecDict

class WordGroup(total: Int) {

  protected var sumVec: Option[INDArray] = None
  protected var counter = 0
  protected val words = new HashMap[String, Int]

  def add(word: String, wvec: INDArray): Unit = {
    words.put(word, words.getOrElseUpdate(word, 0) + 1)
    sumVec = sumVec match {
      case None => Some(wvec)
      case Some(e) => Some(e.addi(wvec))
    }
    counter += 1
  }

  def repr: INDArray = sumVec match {
    case None => null
    case Some(sum) => sum.divi(counter)
  }
}

object PatternExtractor {
  val threshold = 0.1
  val similarity = 0.2
}

class PatternExtractor {

  private val lexer = Lexer

  private val wordFrequency = new HashMap[String, Double]
  private val sections = new ArrayBuffer[Section]

  private val dict = new WordVecDict("/home/harper/Downloads/glove.42B.300d.txt")

  def extract(lines: Seq[String]): Pattern = {

    val tokens = lines.map(lexer.tokenize(_)
      .map(_.content.toLowerCase()).toIndexedSeq)

    // Compute word frequency
    wordFrequency.clear()
    val tokenGroup = tokens.map(_.toSet.toList)
      .flatten.groupBy(k => k).mapValues(_.length).filter(_._2 > 1)
    val lineCount = lines.length
    wordFrequency ++= tokenGroup.mapValues(_.toDouble / lineCount)

    // Look for hot spots in lines
    val hspots = tokens.map(_.map(word => (word, wordFrequency.getOrElse(word, 0d)))
      .filter(_._2 >= PatternExtractor.threshold).map(_._1).toArray).toArray

    // Align hot spots. This looks for an assignment that minimize the entropy
    align(hspots)

    null
  }

  def align(hotspots: Array[Array[String]]): Array[Set[String]] = {
    val numLine = hotspots.length
    val groups = new ArrayBuffer[WordGroup]

    hotspots.foreach(hs => {
      val hsval = hs.map(k => (k, dict.find(k)))
        .filter(!_._2.isEmpty).map(p => (p._1, p._2.get))
      val grpval = groups.map(_.repr)
      val assign = similar(hsval.map(_._2), grpval)
      var acounter = 0
      var bcounter = 0
      val newgroups = new ArrayBuffer[WordGroup]
      assign.foreach(_ match {
        case 1 => { // Add a new group
          val ng = new WordGroup(numLine)
          val hswd = hsval(acounter)
          ng.add(hswd._1, hswd._2)
          acounter += 1
          newgroups += ng
        }
        case 2 => bcounter += 1
        case 3 => {
          acounter += 1
          bcounter += 1
        }
      })
    })

    null
  }

  /**
   * Find a combination that maximize the similarity
   *
   * @return an array representing the assignment.
   * 					1 => The value is from a
   * 					2 => The value is from b
   * 					3 => The value in a and b match here
   */
  def similar(a: IndexedSeq[INDArray], b: IndexedSeq[INDArray]): Array[Int] = {
    val maxdist = a.length + b.length + 1
    val store = (0 to maxdist).map(i => new Array[Double](maxdist)).toArray
    val path = (0 to maxdist).map(i => new Array[(Int, Int)](maxdist)).toArray

    (0 to maxdist).foreach(i => {
      store(0)(i) = 0
      path(0)(i) = (0, 1)
      store(i)(0) = 0
      path(i)(0) = (1, 0)
    })

    null
  }
}