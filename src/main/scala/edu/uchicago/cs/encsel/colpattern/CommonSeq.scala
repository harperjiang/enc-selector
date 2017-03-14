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

import java_cup.runtime._

import edu.uchicago.cs.encsel.colpattern.lexer.Sym

import scala.collection.mutable.ArrayBuffer


/**
  * Created by Hao Jiang on 3/14/17.
  */
object CommonSeq {

  val sequence_length = 3

  implicit def bool2int(b: Boolean) = if (b) 1 else 0

  /**
    * Find Common sub-sequence in two sequences
    *
    * This method will choose longer sequence for two overlapped ones.
    *
    * @param a
    * @param b
    * @return sequence of common symbols with length >= 2
    */
  def find(a: Seq[Symbol], b: Seq[Symbol]): Seq[Seq[Symbol]] = {
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
    candidates.map(f => {
      (f._1 until f._1 + f._3).map(a(_))
    })
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
    */
  def extract(lines: Seq[String]): Seq[Seq[Symbol]] = {
    val symlines = lines.map(lexer.Scanner.scan(_).toSeq)

    val commons = new ArrayBuffer[Seq[Symbol]]
    commons += symlines(0)
    symlines.drop(1).foreach(symline => {
      val newcommons = commons.map(CommonSeq.find(_, symline)).flatten.toArray
      commons.clear
      commons ++= newcommons
    })
    commons
  }
}
