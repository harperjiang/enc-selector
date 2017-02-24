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

package edu.uchicago.cs.encsel.app

import java.io.FileInputStream

import edu.uchicago.cs.encsel.dataset.parser.csv.CommonsCSVParser
import edu.uchicago.cs.encsel.dataset.schema.Schema
import edu.uchicago.cs.encsel.model.DataType

import scala.collection.JavaConversions._
import java.nio.file.Paths
import java.nio.file.Files
import java.nio.file.Path
import java.io.File
import java.net.URI

object CompareWithCarbon extends App {

  val csvfile = "/home/harper/enc_workspace/carbon/dict_is_good.csv"
  val schema = new Schema(Array((DataType.STRING, "id"), (DataType.STRING, "loc"), (DataType.STRING, "type"), (DataType.DOUBLE, "psize"), (DataType.DOUBLE, "dsize")), true)

  compareSize

  def genCarbonSQL: Unit = {
    val parser = new CommonsCSVParser
    val records = parser.parse(new FileInputStream(csvfile), schema)
    val sql1 = """cc.sql("create table if not exists tab_%d (id string) STORED BY 'carbondata'")"""
    val sql2 = """cc.sql("load data inpath '/home/harper/enc_workspace/carbon/%d.tmp' into table tab_%d")"""

    records.foreach(rec => {
      val id = rec(0).toInt
      println(sql1.format(id))
      println(sql2.format(id, id))
    })
  }

  def compareSize: Unit = {
    val parser = new CommonsCSVParser
    val records = parser.parse(new FileInputStream(csvfile), schema)
    val carbondir = Paths.get(new File("/home/harper/Repositories/incubator-carbondata/bin/carbonshellstore/default").toURI)

    records.foreach {
      rec =>
        {
          val id = rec(0)
          val tabledir = carbondir.resolve("tab_" + id)
          val tablesize = folderSize(tabledir)
          val plainsize = rec(3)
          val dictsize = rec(4)
          val uri = Paths.get(new URI(rec(1)))
          println("%s,%s,%s,%s,%d".format(id,uri.getFileName.toString(), plainsize, dictsize, tablesize))
        }
    }
  }

  def folderSize(folder: Path): Long = {
    Files.walk(folder).iterator().filter { !Files.isDirectory(_) }.map(p => new File(p.toUri).length()).sum
  }
}