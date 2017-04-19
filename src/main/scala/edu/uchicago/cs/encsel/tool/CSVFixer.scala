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

import java.io.{File, StringReader}
import java.net.URI

import org.apache.commons.csv.CSVFormat

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * Fix malformated CSV files
  */


object FixCSV extends App {
  new CSVFixer(13).fix(new File("/home/harper/dataset/temp/prop_mgt_vio_after2015.csv").toURI).foreach(println)
}

class CSVFixer(val expectColumn: Int) {

  val buffer = new ArrayBuffer[String]

  def fix(file: URI): Iterator[String] = {
    Source.fromFile(file).getLines().map(line => {
      buffer += line
      if (buffer.last.endsWith("\"")) {
        val fixed = new CSVLineFixer(expectColumn).fixLine(buffer.mkString("\n"))
        buffer.clear
        fixed
      } else {
        ""
      }
    }).filter(!_.isEmpty)
  }
}

class CSVLineFixer(expectCol: Int) {
  val plain: (String) => String = { input =>
    try {
      val records = CSVFormat.EXCEL.parse(new StringReader(input)).getRecords
      records(0).toString
    } catch {
      case e: Exception => {
        ""
      }
    }
  }

  val escape: (String) => String = { input =>
    // It's hard to use regex, so just directly loop

    val buffer = new StringBuffer

    input.zipWithIndex.foreach(p => {
      val char = p._1
      val index = p._2

      if (char == '\"') {
        // Not head or bottom
        if (index != 0 && index != input.length - 1) {
          if (input(index - 1) != ',' && input(index + 1) != ','
            && input(index - 1) != '\"' && input(index + 1) != '\"') {
            buffer.append("\"\"")
          } else {
            buffer.append(char)
          }
        } else {
          buffer.append(char)
        }
      } else {
        buffer.append(char)
      }
    })

    // Find match of double quotes, and escape those missed
    val mark = new mutable.HashSet[Int]
    buffer.toString.zipWithIndex.foreach(p => {
      val char = p._1
      val index = p._2
      var state = 0
      if (char == '\"') {
        state match {
          case 0 => {
            if (index == 0 || prev(buffer, index - 1) == ',') {
              state = 1
              mark += index
            }
          }
          case 1 => {
            if (index == buffer.length - 1 || next(buffer, index + 1) == ',') {
              state = 0
              mark += index
            }
          }
        }
      }
    })

    // Escape those not marked
    val newbuffer = new StringBuffer()
    buffer.toString.zipWithIndex.foreach(p => {
      val char = p._1
      val index = p._2
      if (mark.contains(index)) {
        newbuffer.append("\"\"")
      } else {
        newbuffer.append(char)
      }
    })

    try {
      val records = CSVFormat.EXCEL.parse(new StringReader(newbuffer.toString)).getRecords
      records(0).toString
    } catch {
      case e: Exception => {
        ""
      }
    }
  }

  def prev(buffer: StringBuffer, idx: Int): Char = {
    buffer.charAt(idx - 1)
  }

  def next(buffer: StringBuffer, idx: Int): Char = {

    buffer.charAt(idx + 1)
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