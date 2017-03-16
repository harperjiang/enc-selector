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

import java_cup.runtime._

import edu.uchicago.cs.encsel.ptnmining.lexer.Sym

import scala.collection.mutable.ArrayBuffer


/**
  * Created by Hao Jiang on 3/14/17.
  */
object CommonSeq {

  val sequence_length = 3
  // Percentage that a common sequence is not in some sentence
  // TODO: the tolerance is not supported now
  val tolerance = 0.1

  implicit def bool2int(b: Boolean) = if (b) 1 else 0

  /**
    * Find Common sub-sequence in two sequences
    *
    * This method will choose longer sequence for two overlapped sequences.
    *
    * @param a the first sequence
    * @param b the second sequence
    * @return sequence of common symbols with length >= <code>sequence_length</code>
    */
  def find(a: Seq[Symbol], b: Seq[Symbol]): Seq[(Int, Int, Int)] = {
    val data = a.indices.map(i => new Array[Int](b.length))
    a.indices.foreach(i => data(i)(0) = equalSymbol(a(i), b(0)))
    b.indices.foreach(i => data(0)(i) = equalSymbol(a(0), b(i)))

    val candidates = new ArrayBuffer[(Int, Int, Int)]
    for (i <- 1 until a.length; j <- 1 until b.length) {
      data(i)(j) = equalSymbol(a(i), b(j)) match {
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
    not_overlap
  }

  protected def equalSymbol(a: Symbol, b: Symbol): Boolean = {
    a.sym match {
      case wd if wd == b.sym && wd == Sym.WORD => a.value.equals(b.value)
      case neq if neq != b.sym => false
      case _ => true
    }
  }
}


class CommonSeq {

  /**
    * Look for common sequences that comprised of only numbers
    * and separators
    *
    * @param lines
    * @return common sequences
    */
  def extract(lines: Seq[String]): Seq[Seq[Symbol]] = {
    val symlines = lines.map(lexer.Scanner.scan(_).toSeq)

    val commons = new ArrayBuffer[Seq[Symbol]]
    commons += symlines(0)
    symlines.drop(1).foreach(symline => {
      val newcommons = new ArrayBuffer[Seq[Symbol]]
      // Commons sorted by length
      val subseqs = commons.map(CommonSeq.find(_, symline)).flatten.sortBy(-_._3)
      // Make sure they don't overlap on the new line
      val pholder = Array.fill(symline.length)(0)
      subseqs.foreach(ss => {
        if (pholder.slice(ss._1, ss._1 + ss._3).toSet.filter(_ >= ss._3).size == 0) {
          (ss._1 until ss._1 + ss._3).foreach(pholder(_) = ss._3)
          newcommons += symline.slice(ss._2, ss._2 + ss._3)
        }
      })
      commons.clear
      commons ++= newcommons
    })
    commons
  }
}
