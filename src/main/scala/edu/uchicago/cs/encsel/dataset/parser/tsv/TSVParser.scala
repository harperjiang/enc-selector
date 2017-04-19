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
package edu.uchicago.cs.encsel.dataset.parser.tsv

import java.io.{BufferedInputStream, InputStream, InputStreamReader, Reader}

import edu.uchicago.cs.encsel.dataset.parser.{DefaultRecord, Parser, Record}
import edu.uchicago.cs.encsel.dataset.schema.Schema

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

/**
  * TSVParser supports double quote escaped records
  */
class SimpleTSVParser extends Parser {

  override def parseLine(line: String): Record = {
    line.trim.isEmpty match {
      case true => Record.EMPTY
      // Single tab,
      case false => new DefaultRecord(line.split("\t", -1))
    }
  }

  protected override def guessHeader(line: String): Unit = {
    guessedHeader = line.split("\t")
  }
}

class TSVParser extends Parser {

  override def parse(input: InputStream, schema: Schema): Iterator[Record] = {
    this.schema = schema

    val bufferedinput = new BufferedInputStream(input)
    // Guess Encoding
    val encoding = guessEncoding(bufferedinput)

    val lines = Source.fromInputStream(bufferedinput, encoding).getLines()

    if (null == schema) {
      // Guess header, need to retrieve a line
      val line = lines.next()
      guessHeader(line)
    } else if (schema.hasHeader) {
      // For simplicity, here we assume the header line is a single line
      lines.next()
    }
    val reader = new InputStreamReader(bufferedinput, encoding)
    parseRecords(reader)
  }

  def parseRecords(reader: Reader): Iterator[Record] =
    new Iterator[Record] {

      var nextRecord: Option[Record] = readNextRecord(reader)

      override def hasNext: Boolean = nextRecord.isDefined

      override def next(): Record = {
        nextRecord.isDefined match {
          case true => {
            val toReturn = nextRecord.get
            nextRecord = readNextRecord(reader)
            toReturn
          }
          case false => throw new IllegalStateException
        }
      }
    }

  def readNextRecord(reader: Reader): Option[Record] = {
    var buffer = new ArrayBuffer[String]()
    var stop = false
    var state = 0
    while (!stop) {
      reader.read() match {
        case -1 => {
          // No more data, end record
          stop = true
        }
        case '\n' => {

        }
        case '\r' => {

        }
        case '\"' => {

        }
        case '\t' => {

        }
      }
    }
    buffer.length match {
      case 0 => None
      case _ => Some(new DefaultRecord(buffer.toArray))
    }
  }
}