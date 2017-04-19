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
 *
 */

package edu.uchicago.cs.encsel.tool

import java.io._
import java.net.URI

import org.apache.commons.csv.{CSVFormat, CSVRecord}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Fix malformated CSV files
  */


object FixCSV extends App {
  val output = new PrintWriter(new FileOutputStream(args(0) + ".fixed"))
  val bad = new PrintWriter(new FileOutputStream(args(0) + ".bad"))
  new CSVFixer(args(1).toInt).fix(new File(args(0)).toURI).foreach(p => {
    p._2 match {
      case true => output.println(p._1)
      case false => bad.println(p._1)
    }
  })
  output.close
  bad.close
}

/**
  * Filter data
  *
  * @param inner
  */
class FilterStream(val inner: InputStream) extends java.io.InputStream {
  override def read(): Int = {
    var char = inner.read()
    while (char > 127 || char == 0) {
      char = inner.read()
    }
    char
  }

  override def available(): Int = inner.available()
}

class CSVFixer(val expectColumn: Int) {

  val buffer = new ArrayBuffer[String]

  def fix(file: URI): Iterator[(String, Boolean)] = {
    Source.fromInputStream(new FilterStream(new FileInputStream(new File(file))), "utf-8")
      .getLines().map(line => {
      buffer += line
      if (buffer.last.endsWith("\"")) {
        val tofix = buffer.mkString(" ")
        val fixed = new CSVLineFixer(expectColumn).fixLine(tofix)
        buffer.clear
        fixed.isEmpty match {
          case true => (tofix, false)
          case false => (fixed, true)
        }
      } else {
        ("", true)
      }
    }).filter(p => !p._2 || !p._1.isEmpty)
  }
}

class CSVLineFixer(expectCol: Int) {

  val plain: (String) => String = { input =>
    try {
      CSVFormat.EXCEL.parse(new StringReader(input)).getRecords
      input
    } catch {
      case e: Exception => {
        ""
      }
    }
  }

  val consecDoubleQuote = """"+""".r

  val escape: (String) => String = { input =>
    // It's hard to use regex, so just directly loop
    val buffer = new StringBuffer

    // Find match of double quotes, and escape those missed
    val mark = new mutable.HashSet[Int]
    var state = 0
    input.zipWithIndex.foreach(p => {
      val char = p._1
      val index = p._2
      char match {
        case '\"' => {
          state match {
            case 0 => {
              if (index == 0 || prev(input, index) == ',') {
                state = 1
                mark += index
              }
            }
            case 1 => {
              if (index == input.length - 1 || next(input, index) == ',') {
                state = 0
                mark += index
              }
            }
            case 2 => Unit
          }
        }
        case '<' => {
          state = 2
        }
        case '>' => {
          state = 1
        }
        case _ => Unit
      }
    })

    consecDoubleQuote.findAllMatchIn(input).foreach(mch => {
      val range = (mch.start until mch.end)

      val unmarked = range.filter(i => !mark.contains(i))

      // Escape a odd block
      if (unmarked.size % 2 != 0) {
        mark ++= unmarked.drop(1)
      } else {
        mark ++= unmarked
      }
    })

    // Escape those not marked
    input.zipWithIndex.foreach(p => {
      val char = p._1
      val index = p._2
      buffer.append(char)
      if (char == '\"' && !mark.contains(index))
        buffer.append("\"")
    })

    try {
      val escaped = buffer.toString
      CSVFormat.EXCEL.parse(new StringReader(escaped)).getRecords
      escaped
    } catch {
      case e: Exception => {
        ""
      }
    }
  }

  def prev(buffer: String, idx: Int): Char = {
    var pnt = idx - 1
    while (buffer(pnt) == ' ')
      pnt -= 1
    buffer.charAt(pnt)
  }

  def next(buffer: String, idx: Int): Char = {
    var pnt = idx + 1
    while (buffer(pnt) == ' ')
      pnt += 1
    buffer.charAt(pnt)
  }


  val methods = Seq(plain, escape)

  def fixLine(input: String): String = {
    var pointer = 0
    var found = false
    var res = ""
    while (pointer < methods.length && !found) {
      res = methods(pointer).apply(input)
      found = !res.isEmpty
      pointer += 1
    }
    res
  }


}

object State {
  val LINE_BEGIN = 0
  val REC_BEGIN = 1
  val REC_IN = 2
  val REC_END = 3
}