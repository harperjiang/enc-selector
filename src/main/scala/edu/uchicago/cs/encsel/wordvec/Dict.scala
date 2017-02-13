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

import edu.uchicago.cs.encsel.parser.csv.CSVParser
import java.io.File
import edu.uchicago.cs.encsel.schema.Schema
import edu.uchicago.cs.encsel.model.DataType
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap

object Dict {

  val abbrvMatch = 0.9

  val dictFile = "src/main/word/top_5000.csv"
  val dictSchema = new Schema(Array((DataType.INTEGER, "seq"), (DataType.STRING, "word"), (DataType.STRING, "pos")), true)

  protected var words = new HashMap[Char, ArrayBuffer[(String,Int)]]()
  protected var abbrvs = new HashMap[Char, ArrayBuffer[(String,Int)]]()
  protected var index = new HashSet[String]()
  protected var abbrvIdx = new HashMap[String, String]()
  init()

  def init(): Unit = {
    var parser = new CSVParser()
    var records = parser.parse(new File(dictFile).toURI(), dictSchema)
    
    records.zipWithIndex.foreach { record => 
	{ 
            var word = record._1(1)
	    index += word
	    abbrvIdx += abbrv(word)
	    words.getOrElse(word(0), new ArrayBuffer[(String,Int)]()) += (word,record._2) 
	    abbrvs.getOrElse(word(0), new ArrayBuffer[(String,Int)]()) += (abbrv(word),record._2)
        } 
    }
    words.foreach(_._2.sortBy(_._1))
  }

  protected def abbrv(input: String) = {
    // Remove any non-leading aeiou
    input.replaceAll("""(?!^)[aeiou]""", "")
  }

  /**
   * This algorithm first match full words, then look for abbreviation
   * Finally look for entries having smallest distance
   *
   * @return pair of (string in dictionary, fidelity)
   */
  def lookup(raw: String): (String, Double) = {
    var input = raw.toLowerCase()
    input match {
      case x if (index.contains(x)) => {
        (input, 1)
      }
      case x if (abbrvIdx.contains(x)) => {
        (words(abbrvIdx.getOrElse(x, -1))._1, abbrvMatch)
      }
      case _ => {
        var partial = words.getOrElse(input(0), new ArrayBuffer[(String,Int)]())
            .map(word => (word, WordUtils.levDistance2(input, word))).minBy(_._2)
        var abbrvPartial = abbrv.getOrElse(input(0), new ArrayBuffer[(String,Int)]())
            .map(word => (word, WordUtils.levDistance2(input, word))).minBy(_._2) 
        // TODO Take the frequency of word into account 
	// TODO Normalize the fidelity
        return Array(partial, abbrvPartial).minBy(_._2)._1	
      }
    }

  }
}
