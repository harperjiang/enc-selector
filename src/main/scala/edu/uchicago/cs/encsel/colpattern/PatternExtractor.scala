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
import scala.collection.Set

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.ops.transforms.Transforms

import edu.uchicago.cs.encsel.word.WordVecDict

class WordGroup {

  protected var sumVec: Option[INDArray] = None
  protected var counter = 0
  protected val wordCounter = new HashMap[String, Int]

  def add(word: String, wvec: INDArray): Unit = {
    wordCounter.put(word, wordCounter.getOrElseUpdate(word, 0) + 1)
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

  def words: Set[String] = wordCounter.keySet
}

object PatternExtractor {
  /**
    * Threshold of popular words
    */
  val threshold = 0.1

  /**
    * Find a combination that maximize the similarity using dynamic programming.
    *
    * The algorithm initialize a 2d array <strong>store</strong> of size
    * [a.length + 1, b.length + 1], of which the element [i + 1,j + 1] represents the
    * maximal similarity up to a_i and b_j, and a 2d array <strong>path</strong> of
    * the same size, with elements indicating the matching path up to here.
    *
    * The value in store is caculated as following:
    * store[i,j] = max( store[i, j - 1],
    * store[i - 1, j],
    * store[i - 1, j - 1] + sim(a[i-1],j[i-1]))
    *
    * Case 1 represents a_{i-1} does not participate in match
    * Case 2 represents b_{j-1} does not participate in match
    * Case 3 represents a_{i-1} and b_{j-1} are matched and their contirbution
    * is calculated using the sim function
    *
    * @return an array representing the matching pair. The number in the tuple represents
    *         the index in a and b
    */
  def similar(a: IndexedSeq[INDArray], b: IndexedSeq[INDArray]): IndexedSeq[(Int, Int)] = {
    val store = (0 to a.length).map(i => new Array[Double](b.length + 1)).toArray
    val path = (0 to a.length).map(i => new Array[(Int, Int)](b.length + 1)).toArray

    (0 to a.length).foreach(i => {
      store(i)(0) = 0
      path(i)(0) = (0, 0)
    })
    (0 to b.length).foreach(i => {
      store(0)(i) = 0
      path(0)(i) = (0, 0)
    })

    for (ai <- 1 to a.length; bi <- 1 to b.length) {
      val cosine_sim = Transforms.cosineSim(a(ai - 1), b(bi - 1))
      // Compute the score
      val max = Array(store(ai)(bi - 1), store(ai - 1)(bi),
        store(ai - 1)(bi - 1) + cosine_sim)
        .zipWithIndex.maxBy(_._1)
      store(ai)(bi) = max._1
      // A match is used, record the path
      path(ai)(bi) = max._2 match {
        case 0 => (ai, bi - 1)
        case 1 => (ai - 1, bi)
        case 2 => (ai - 1, bi - 1)
        case _ => throw new IllegalArgumentException("Unexpected Option")
      }
    }

    // Look backward for path
    val maxpath = new ArrayBuffer[(Int, Int)]
    var apointer = a.length
    var bpointer = b.length

    while (apointer > 0 && bpointer > 0) {
      val next = path(apointer)(bpointer)
      if (next == (apointer - 1, bpointer - 1)) {
        maxpath.insert(0, (apointer - 1, bpointer - 1))
      }
      apointer = next._1
      bpointer = next._2
    }
    maxpath
  }
}

class PatternExtractor {

  private val lexer = Lexer

  private val wordFrequency = new HashMap[String, Double]
  private val sections = new ArrayBuffer[Section]

  private val dict = new WordVecDict("/home/harper/Downloads/glove.840B.300d.txt")

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
    val aligned = align(hspots)

    null
  }

  def align(hotspots: Array[Array[String]]): IndexedSeq[Set[String]] = {
    val numLine = hotspots.length
    val groups = new ArrayBuffer[WordGroup]

    hotspots.foreach(hs => {
      val hsval = hs.map(k => (k, dict.find(k)))
        .filter(!_._2.isEmpty).map(p => (p._1, p._2.get))
      val grpval = groups.map(_.repr)
      val assign = PatternExtractor.similar(hsval.map(_._2), grpval)
      val newgroups = new ArrayBuffer[WordGroup]

      var apointer = 0
      var bpointer = 0

      assign.foreach(pair => {
        val hsi = pair._1
        val grpi = pair._2
        newgroups ++=
          (apointer until hsi).map { i => {
            val newgroup = new WordGroup
            newgroup.add(hsval(i)._1, hsval(i)._2)
            newgroup
          }
          }
        newgroups ++=
          (bpointer until grpi).map(groups(_))
        val newwd = hsval(hsi)
        val grp = groups(grpi)
        grp.add(newwd._1, newwd._2)
        newgroups += grp
        apointer = hsi + 1
        bpointer = grpi + 1
      })
      newgroups ++=
        (apointer until hsval.length).map { i => {
          val newgroup = new WordGroup
          newgroup.add(hsval(i)._1, hsval(i)._2)
          newgroup
        }
        }
      newgroups ++=
        (bpointer until groups.length).map(groups(_))
      groups.clear()
      groups ++= newgroups
    })
    groups.map(_.words)
  }
}