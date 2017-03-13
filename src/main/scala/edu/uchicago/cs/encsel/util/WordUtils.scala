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
import org.nd4j.linalg.api.ndarray.INDArray
import scala.collection.mutable.ArrayBuffer
import org.nd4j.linalg.ops.transforms.Transforms

object WordUtils {

  def levDistance(a: String, b: String): Int = {
    val matrix = Array[Array[Int]]((0 to a.length).map(_ => new Array[Int](b.length + 1)): _*)
    matrix(0)(0) = 0
    for (i <- 1 to a.length) matrix(i)(0) = i
    for (j <- 1 to b.length) matrix(0)(j) = j

    for (j <- 1 to b.length; i <- 1 to a.length) {
      val substcost = (a(i - 1) == b(j - 1)) match {
        case true => 0
        case _ => 1
      }
      matrix(i)(j) = Array(matrix(i)(j - 1) + 1, matrix(i - 1)(j) + 1, matrix(i - 1)(j - 1) + substcost).min
    }

    matrix(a.length)(b.length)
  }
  /**
   * Add a weight to lev distance, a difference at position p has a difference 1.15 - tanh (0.15p)
   * This has an effort that the first char has distance 1, the 5th has distance 0.5 and chars after 10 has 0.15
   */
  def levDistance2(a: String, b: String): Double = {
    val matrix = Array[Array[Double]]((0 to a.length).map(_ => new Array[Double](b.length + 1)): _*)
    matrix(0)(0) = 0
    for (i <- 1 to a.length) matrix(i)(0) = matrix(i - 1)(0) + weight(i)
    for (j <- 1 to b.length) matrix(0)(j) = matrix(0)(j - 1) + weight(j)

    for (j <- 1 to b.length; i <- 1 to a.length) {
      val substcost = (a(i - 1) == b(j - 1)) match {
        case true => 0
        case _ => weight(Math.max(i, j))
      }
      matrix(i)(j) = Array(matrix(i)(j - 1) + weight(j), matrix(i - 1)(j) + weight(i), matrix(i - 1)(j - 1) + substcost).min
    }

    matrix(a.length)(b.length)
  }

  protected def weight(idx: Int): Double = Math.exp(-0.05 * idx) + 0.05

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