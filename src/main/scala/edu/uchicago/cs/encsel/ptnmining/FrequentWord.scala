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

package edu.uchicago.cs.encsel.ptnmining

import edu.uchicago.cs.encsel.ptnmining.lexer.{Scanner, Sym}
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.ops.transforms.Transforms

import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.collection.{Set, mutable}


class WordGroup {

  protected val wordCounter = new HashMap[String, Int]
  protected var sumVec: Option[INDArray] = None
  protected var counter = 0

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

object FrequentWord {
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
    * The value in store is calculated as following:
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
  def similar(a: Seq[INDArray], b: Seq[INDArray]): Seq[(Int, Int)] = {
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

class FrequentWord {

  private val wordFrequency = new HashMap[String, Double]

  private val dict = new WordEmbedDict("/home/harper/Downloads/glove.840B.300d.txt")

  def discover(lines: Seq[String]): Pattern = {
    // Parse sentences to tokens
    val tokens = lines.map(Scanner.scan(_)
      .filter(sym => sym.sym <= Sym.WORD)
      .map(_.value.toString.toLowerCase()).toSeq)

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
      .filter(_._3 >= FrequentWord.threshold))

    // Merge adjacent hotspots
    val merged = merge(hspots)

    // Group hotspots words. This looks for an assignment that maximize the word similarity
    val grouped = group(merged)

    build(grouped)
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
        FrequentWord.children(words).foreach(p =>
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
      .filter(_._2 >= FrequentWord.threshold)

    val combine = () => {
      val words = phraseBuffer.map(_._1).toArray
      phraseBuffer.clear
      if (words.length <= 1)
        words
      else {
        // Choose the longest one when multiple words combinations are available
        // Alternative, choose the most popular
        val children = FrequentWord.children(words)
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
      val assign = FrequentWord.similar(hsval.map(_._2), grpval)
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

  def build(groups: Seq[Set[String]]): Pattern = {
    val regstr = new StringBuilder

    regstr.append("^.*")

    groups.foreach(group => {
      regstr.append("(%s)".format(group.mkString("|")))
      regstr.append(".*")
    })
    regstr.append("$")

    new RegexPattern(regstr.toString, groups.length)
  }
}