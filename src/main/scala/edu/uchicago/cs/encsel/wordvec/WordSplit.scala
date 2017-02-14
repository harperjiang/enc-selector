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
package edu.uchicago.cs.encsel.wordvec

import scala.collection.mutable.Buffer
import org.apache.commons.lang.StringUtils
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import scala.collection.mutable.HashSet

class WordSplit {

  private val vowel = Set('a', 'e', 'i', 'o', 'u').toSeq

  def split(input: String): (Buffer[String], Double) = {
    input match {
      case x if x.contains("_") => {
        // Separator
        var parts = x.split("_")
        var fidelity = 1d
        (parts.map(part => { var lookup = Dict.lookup(part); fidelity *= lookup._2; lookup._1 })
          .filter(StringUtils.isNotEmpty(_)).toBuffer, fidelity)
      }
      case x if !x.equals(x.toUpperCase()) && !x.equals(x.toLowerCase()) => {
        // Camel style
        split(x.replaceAll("([A-Z])", "_\1"))
      }
      case _ => {
        //        splitMemory.clear
        //        var words = wordSplit(input, 0, input.length)
        //        words match {
        //          case null => {
        guessMemory.clear
        guessSplit(input, 0, input.length)
        //          }
        //          case _ => (words, 1)
        //        }
      }
    }
  }

  /**
   * Look for words combination
   */
  protected var splitMemory = new HashMap[(Int, Int), Buffer[String]]()

  def wordSplit(input: String, fromPos: Int, toPos: Int): Buffer[String] = {
    splitMemory.getOrElseUpdate((fromPos, toPos), {
      (fromPos, toPos) match {
        case (f, t) if f == t => new ArrayBuffer[String]()
        case _ => (fromPos + 1 to toPos).map { i =>
          {
            input.substring(fromPos, i) match {
              case x if Dict.strictLookup(x) =>
                wordSplit(input, i, toPos) match {
                  case none if none == null => null
                  case remain => x +: remain
                }
              case _ => null
            }
          }
        }.filter(_ != null) match {
          case empty if empty.isEmpty => null
          case nonEmpty => nonEmpty.minBy(_.length)
        }
      }
    })
  }

  /**
   * Dynamic Programming for Guess abbreviation
   */
  protected var guessMemory = new HashMap[(Int, Int), (Buffer[String], Double)]()

  def guessSplit(input: String, fromPos: Int, toPos: Int): (Buffer[String], Double) = {
    guessMemory.getOrElseUpdate((fromPos, toPos), {
      // Scan and recognize
      (fromPos, toPos) match {
        case (f, t) if f >= t => (ArrayBuffer.empty[String], 1)
        case (f, t) if f == t - 1 => {
          var lookup = Dict.lookup(input.substring(fromPos, toPos))
          (ArrayBuffer(lookup._1), lookup._2)
        }
        case _ =>
          (fromPos + 1 to toPos).map(i => {
            var left = Dict.lookup(input.substring(fromPos, i))
            var right = guessSplit(input, i, toPos)
            (left._1 +: right._1, left._2 * right._2)
          }).maxBy(t => (t._2, -t._1.length))
      }
    })
  }
}