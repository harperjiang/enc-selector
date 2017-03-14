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

import scala.collection.{Set, mutable}
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import org.nd4j.linalg.api.ndarray.INDArray
import edu.uchicago.cs.encsel.util.WordUtils


class WordGroup {

  protected var sumVec: Option[INDArray] = None
  protected var counter = 0
  protected val wordCounter = new HashMap[String, Int]

  def add(word: String, wvec: INDArray): Unit = {
    wordCounter.put(word, wordCounter.getOrElseUpdate(word, 0) + 1)
    sumVec = sumVec match {
      case None => Some(wvec)
      case Some(e) => Some(e.add(wvec))
    }
    counter += 1
  }

  def repr: INDArray = sumVec match {
    case None => null
    case Some(sum) => sum.div(counter)
  }

  def words: Set[String] = wordCounter.keySet
}

object Phrase {
  /**
    * Find all children combinations of the words(at least 2), long to short
    *
    * @param words
    * @return
    */
  def children(words: Array[String]): Iterator[(Seq[String], Range)] =
    new Iterator[(Seq[String], Range)]() {
      val buffer = new mutable.Queue[Range]
      val unique = new mutable.HashSet[Range]
      buffer.enqueue(words.indices)
      unique += words.indices

      override def next() = {
        val item = buffer.dequeue
        unique.remove(item)
        if (item.length > 2) {
          val left = item.dropRight(1)
          if (!unique.contains(left)) {
            buffer.enqueue(left)
            unique += left
          }
          val right = item.drop(1)
          if (!unique.contains(right)) {
            buffer.enqueue(right)
            unique += right
          }
        }
        (item.map(words(_)), item)
      }

      override def hasNext = !buffer.isEmpty
    }
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

  private val dict = new WordEmbedDict("/home/harper/Downloads/glove.840B.300d.txt")

  def extract(lines: Seq[String]): Pattern = {
    // Parse sentences to tokens
    val tokens = lines.map(lexer.tokenize(_)
      .map(_.content.toLowerCase()).toSeq)

    // Compute word frequency
    // Multiple words in each sentence are counted once
    // Only count words appear in more than one sentences
    val tokenGroup = tokens.map(_.toSet.toList)
      .flatten.groupBy(k => k).mapValues(_.length).filter(_._2 > 1)
    val lineCount = lines.length
    wordFrequency.clear
    wordFrequency ++= tokenGroup.mapValues(_.toDouble / lineCount)

    // Look for hot spots in lines
    val hspots = tokens.map(_.zipWithIndex.map(word =>
      (word._1, word._2, wordFrequency.getOrElse(word._1, 0d)))
      .filter(_._3 >= PatternFinder.threshold))

    // Merge adjacent hotspots
    val merged = merge(hspots)

    // Group hotspots words. This looks for an assignment that maximize the word similarity
    val grouped = group(merged)

    null
  }

  /**
    * Merge adjacent hotspots with high correlation together into phrases
    *
    * To determine whether words should be combined, we compute the frequency of all phrases.
    * If the frequency of a phrase is higher enough, we recognize it as an independent
    * hotspot and replace all occurrence
    *
    * @param hspots (word, index, frequency) in each line
    * @return
    */
  def merge(hspots: Seq[Seq[(String, Int, Double)]]): Seq[Seq[String]] = {
    val phraseCounter = new HashMap[Seq[String], Double]
    val phraseBuffer = new ArrayBuffer[(String, Int)]

    val record = () => {
      val words = phraseBuffer.map(_._1).toArray
      if (words.length > 1)
        Phrase.children(words).foreach(p =>
          phraseCounter.put(p._1, phraseCounter.getOrElseUpdate(p._1, 0) + 1))
      phraseBuffer.clear
    }
    // Record common phrases
    hspots.foreach(line => {
      line.foreach(word => {
        if (!phraseBuffer.isEmpty && phraseBuffer.last._2 != word._2 - 1) // non-adjacent words
          record()
        phraseBuffer += ((word._1, word._2))
      })
      record()
    })
    val validPhrase = phraseCounter.mapValues(_ / hspots.length)
      .filter(_._2 >= PatternFinder.threshold)

    val combine = () => {
      val words = phraseBuffer.map(_._1).toArray
      phraseBuffer.clear
      if (words.length <= 1)
        words
      else {
        // Choose the longest one when multiple words combinations are available
        // Alternative, choose the most popular
        val children = Phrase.children(words)
        val valid = children.find(p => validPhrase.contains(p._1))
        valid match {
          case None => words
          case Some(e) => {
            val range = e._2
            val before = (0 until range.start).map(words(_))
            val after = (range.end until words.length).map(words(_)).toArray
            before ++: Array(e._1.mkString(" ")) ++: after
          }
        }
      }
    }
    hspots.map(line => {
      val combined = new ArrayBuffer[String]
      line.foreach(word => {
        if (!phraseBuffer.isEmpty && phraseBuffer.last._2 != word._2 - 1)
          combined ++= combine()
        phraseBuffer += ((word._1, word._2))
      })
      combined ++= combine()
    })
  }

  /**
    * Group hotspots by their similarity.
    *
    * @param hotspots hotspot words in each sentence
    * @return hotspot words separated into groups
    */
  def group(hotspots: Seq[Seq[String]]): Seq[Set[String]] = {
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