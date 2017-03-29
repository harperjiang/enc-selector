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

package edu.uchicago.cs.encsel.ptnmining.rule

import scala.collection.mutable.ArrayBuffer


/**
  * Created by Hao Jiang on 3/14/17.
  */
class CommonSeq {

  val sequence_length = 2
  // Percentage that a common sequence is not in some sentence
  // TODO: the tolerance is not supported now
  val tolerance = 0.1

  implicit def bool2int(b: Boolean) = if (b) 1 else 0

  val positions = new ArrayBuffer[Seq[(Int, Int)]]

  /**
    * Look for common sequences that comprised of only numbers
    * and separators
    *
    * @param lines
    * @return common sequences
    */
  def find[T](lines: Seq[Seq[T]], equal: (T, T) => Boolean): Seq[Seq[T]] = {
    positions.clear
    val commons = new ArrayBuffer[Seq[T]]
    commons += lines(0)
    lines.drop(1).foreach(line => {
      val newcommons = new ArrayBuffer[Seq[T]]
      // Commons sorted by length
      val subseqs = commons.map(between(_, line, equal)).flatten.sortBy(-_._3)
      // Make sure they don't overlap on the new line
      val pholder = Array.fill(line.length)(0)
      val nonOverlap = new ArrayBuffer[(Int, Int, Int)]
      subseqs.foreach(ss => {
        if (pholder.slice(ss._2, ss._2 + ss._3).toSet.filter(_ >= ss._3).size == 0) {
          (ss._2 until ss._2 + ss._3).foreach(pholder(_) = ss._3)
          nonOverlap += ss
          newcommons += line.slice(ss._2, ss._2 + ss._3)
        }
      })
      commons.clear
      commons ++= newcommons
      if (positions.length == 0) {
        positions += nonOverlap.map(p => (p._1, p._3)).sortBy(_._1)
      }
      positions += nonOverlap.map(p => (p._2, p._3)).sortBy(_._1)
    })
    commons
  }

  /**
    * Find Common sub-sequence in two sequences
    *
    * This method will choose longer sequence for two overlapped sequences.
    *
    * @param a     the first sequence
    * @param b     the second sequence
    * @param equal equality test function
    * @return sequence of common symbols with length >= <code>sequence_length</code>.
    *         (a_start, b_start, length)
    */
  def between[T](a: Seq[T], b: Seq[T], equal: (T, T) => Boolean): Seq[(Int, Int, Int)] = {
    val data = a.indices.map(i => new Array[Int](b.length))
    a.indices.foreach(i => data(i)(0) = equal(a(i), b(0)))
    b.indices.foreach(i => data(0)(i) = equal(a(0), b(i)))

    val candidates = new ArrayBuffer[(Int, Int, Int)]
    for (i <- 1 until a.length; j <- 1 until b.length) {
      data(i)(j) = equal(a(i), b(j)) match {
        case true => {
          if ((i == a.length - 1 || j == b.length - 1)
            && data(i - 1)(j - 1) >= sequence_length - 1) {
            val len = data(i - 1)(j - 1)
            candidates += ((i - len, j - len, len + 1))
          }
          data(i - 1)(j - 1) + 1
        }
        case false => {
          if (data(i - 1)(j - 1) >= sequence_length) {
            val len = data(i - 1)(j - 1)
            candidates += ((i - len, j - len, len))
          }
          0
        }
      }
    }
    // Removing overlap
    val pha = Array.fill(a.length)(0)
    val phb = Array.fill(b.length)(0)
    val not_overlap = new ArrayBuffer[(Int, Int, Int)]
    // From long to short
    candidates.sortBy(-_._3).foreach(c => {
      val afree = pha.slice(c._1, c._1 + c._3).toSet.filter(_ >= c._3).size == 0
      val bfree = phb.slice(c._2, c._2 + c._3).toSet.filter(_ >= c._3).size == 0
      if (afree && bfree) {
        not_overlap += c
        (c._1 until c._1 + c._3).foreach(pha(_) = c._3)
        (c._2 until c._2 + c._3).foreach(phb(_) = c._3)
      }
    })
    not_overlap.sortBy(_._1)
  }

}

