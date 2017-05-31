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

package edu.uchicago.cs.encsel.ptnmining.gen

import java.io.{FileWriter, PrintWriter}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

/**
  * Created by harper on 5/31/17.
  */
class DataGen extends App {

  def range(from: Int, to: Int, padding: Int): IndexedSeq[String] = {
    val format = "%0" + padding + "d"
    (from to to).map(format.format(_))
  }

  def pattern(data: String, pattern: String): String = {

  }

  def genDate(copy: Int): Iterable[String] = {
    val longyears = range(1900, 2050, 4)
    val shortyears = range(0, 99, 2)
    val months = range(0, 12, 2)
    val days = range(0, 30, 2)


    (0 until copy).flatMap(i => {
      val longyear = longyears(Random.nextInt(longyears.size))
      val shortyear = shortyears(Random.nextInt(shortyears.size))
      val month = months(Random.nextInt(months.size))
      val day = days(Random.nextInt(days.size))

      val result = new ArrayBuffer[String]()

      // pattern 1, yyyy-mm-dd
      val ptn1 = "%d-%d-%d".format(longyear, month, day)
      result += pattern(ptn1, "<NUM> - <NUM> - <NUM>")
      // pattern 2, mm/dd/yyyy
      val ptn2 = "%d/%d/%d".format(month, day, longyear)
      result += pattern(ptn2, "<NUM> / <NUM> / <NUM>")
      // pattern 3, mm/dd/yy
      val ptn3 = "%d/%d/%d".format(month, day, shortyear)
      result += pattern(ptn3, "<NUM> / <NUM> / <NUM>")
      result
    })
  }

  def genTimestamp(copy: Int): Iterable[String] = {

  }

  def genIpAddress(copy: Int): Iterable[String] = {

  }


  val output = "pattern.train"

  val writer = new PrintWriter(new FileWriter(output))

  genDate(5000).foreach(writer.println)
  genTimestamp(5000).foreach(writer.println)
  genIpAddress(5000).foreach(writer.println)

  writer.close()
}
