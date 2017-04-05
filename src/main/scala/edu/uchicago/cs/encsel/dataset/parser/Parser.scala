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
package edu.uchicago.cs.encsel.dataset.parser

import java.net.URI

import scala.io.Source

import org.slf4j.LoggerFactory

import edu.uchicago.cs.encsel.dataset.schema.Schema
import scala.collection.Iterator.JoinIterator
import org.apache.commons.lang.StringUtils
import java.io.InputStream
import java.io.FileInputStream
import java.io.File

trait Parser {

  var schema: Schema = _
  protected var headerInline = false
  protected val logger = LoggerFactory.getLogger(getClass)
  protected var blankRecord: BlankRecord = _

  def parse(input: InputStream, schema: Schema): Iterator[Record] = {
    this.schema = schema

    val lines = Source.fromInputStream(input).getLines()

    if (null == schema) {
      // Guess header, need to retrieve a line
      val line = lines.next()
      guessHeader(line)
      blankRecord = new BlankRecord(guessedHeader.length)
      if (headerInline) {
        // Put the line back
        val lb = Array(line).toIterator ++ (lines)
        return lb.map { parseLineIgnoreError }
      }
    } else {
      blankRecord = new BlankRecord(schema.columns.length)
    }
    lines.map { parseLineIgnoreError }
  }

  def parse(inputFile: URI, schema: Schema): Iterator[Record] = parse(new FileInputStream(new File(inputFile)), schema)

  protected def parseLineIgnoreError(line: String): Record = {
    try {
      line match {
        case x if StringUtils.isEmpty(x) => blankRecord
        case _ => parseLine(line)
      }
    } catch {
      case e: Exception => {
        logger.warn("Exception while parsing line:" + line, e)
        Record.EMPTY
      }
    }
  }

  protected def guessHeader(line: String): Unit = {}
  protected var guessedHeader: Array[String] = _

  def guessHeaderName: Array[String] = guessedHeader

  protected def parseLine(line: String): Record = blankRecord
}