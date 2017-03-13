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

import scala.collection.Set
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

import org.nd4j.linalg.api.ndarray.INDArray

import edu.uchicago.cs.encsel.util.WordUtils
import edu.uchicago.cs.encsel.word.WordEmbedDict


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

object PatternFinder {
  /**
    * Threshold of popular words
    */
  val threshold = 0.1
}

class PatternFinder {

  private val lexer = Lexer

  private val wordFrequency = new HashMap[String, Double]
  private val sections = new ArrayBuffer[Section]

  private val dict = new WordEmbedDict("/home/harper/Downloads/glove.840B.300d.txt")

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
      .filter(_._2 >= PatternFinder.threshold).map(_._1).toArray).toArray

    // Align hot spots. This looks for an assignment that minimize the entropy
    val aligned = align(hspots)

    null
  }

  def align(hotspots: Array[Array[String]]): IndexedSeq[Set[String]] = {
    val groups = new ArrayBuffer[WordGroup]

    hotspots.foreach(hs => {
      val hsval = hs.map(k => (k, dict.find(k)))
        .filter(!_._2.isEmpty).map(p => (p._1, p._2.get))
      val grpval = groups.map(_.repr)
      val assign = WordUtils.similar(hsval.map(_._2), grpval)
      val newgroups = new ArrayBuffer[WordGroup]

      var apointer = 0
      var bpointer = 0

      val createGroup = (i: Int) => {
        val newgroup = new WordGroup
        newgroup.add(hsval(i)._1, hsval(i)._2)
        newgroup
      }

      assign.foreach(pair => {
        val hsi = pair._1
        val grpi = pair._2
        newgroups ++= (apointer until hsi).map(createGroup(_))
        newgroups ++= (bpointer until grpi).map(groups(_))
        val newwd = hsval(hsi)
        val grp = groups(grpi)
        grp.add(newwd._1, newwd._2)
        newgroups += grp
        apointer = hsi + 1
        bpointer = grpi + 1
      })
      newgroups ++= (apointer until hsval.length).map(createGroup(_))
      newgroups ++= (bpointer until groups.length).map(groups(_))
      groups.clear()
      groups ++= newgroups
    })
    groups.map(_.words)
  }
}