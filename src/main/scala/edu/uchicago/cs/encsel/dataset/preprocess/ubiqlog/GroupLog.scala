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

package edu.uchicago.cs.encsel.dataset.preprocess.ubiqlog

import java.io.{File, PrintWriter}
import java.net.URI
import java.nio.file.{Files, Paths}

import com.google.gson.{Gson, JsonParser}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.io.Source

/**
  * The UbiqLog (see README.txt in dataset folder for detail) contains
  * several different types of logs. Separate them and store in different files
  */
object GroupLog extends App {
  //val root = "/local/hajiang/dataset/uci_repo/UbiqLog4UCI"
  val root = "/home/harper/test"

  val jsonParser = new JsonParser()
  val output = new mutable.HashMap[String, PrintWriter]

  scan(new File(root).toURI, process)

  output.values.foreach(_.close)


  def scan(folder: URI, proc: URI => Unit): Unit = {
    Files.list(Paths.get(folder)).iterator().foreach(path => {
      if (path.toFile.isDirectory) {
        scan(path.toUri, proc)
      } else {
        val fname = path.getFileName.toString
        if (fname.startsWith("log") && fname.endsWith("txt")) {
          proc(path.toUri)
        }
      }
    })
  }

  def process(uri: URI): Unit = {
    Source.fromFile(uri, "utf-8").getLines().foreach(line => {
      // Escape double double quote
      val formatted = line.replaceAll("\"\"","\\\\\"\"")
      try {
        val json = jsonParser.parse(formatted).getAsJsonObject
        val entrySet = json.entrySet()
        entrySet.foreach(e => {
          val key = e.getKey
          val value = e.getValue
          output.getOrElseUpdate(key, new PrintWriter(root + "/" + key + ".json"))
            .println(value.toString)
        })
      } catch {
        case e: Exception => {
          println(line)
          println(formatted)
        }
      }
    })
  }
}
