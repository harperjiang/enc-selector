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
package edu.uchicago.cs.encsel.parser

import java.net.URI

import scala.io.Source

import org.slf4j.LoggerFactory

import edu.uchicago.cs.encsel.schema.Schema

trait Parser {

  var schema: Schema = null

  var logger = LoggerFactory.getLogger(getClass())

  def parse(inputFile: URI, schema: Schema): Iterable[Record] = {
    this.schema = schema
    return Source.fromFile(inputFile).getLines()
      .filter(!_.trim().isEmpty())
      .map { parseLineIgnoreError(_) }.toIterable
  }

  def parse(inputString: String, schema: Schema): Iterable[Record] = {
    this.schema = schema
    return inputString.split("[\r\n]+").map { parseLineIgnoreError(_) }.toIterable
  }

  def parseLineIgnoreError(line: String): Record = {
    try {
      return parseLine(line)
    } catch {
      case e: Exception => {
        logger.warn("Exception while parsing line:" + line, e)
        return Record.EMPTY
      }
    }
  }

  def parseLine(line: String): Record = Record.EMPTY

  def guessHeaderName(): Array[String] = Array[String]();
}