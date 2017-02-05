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
package edu.uchicago.cs.encsel.parser.csv

import edu.uchicago.cs.encsel.parser.Parser
import edu.uchicago.cs.encsel.schema.Schema
import scala.io.Source
import edu.uchicago.cs.encsel.parser.Record
import java.net.URI
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.io.FileReader
import org.apache.commons.csv.CSVRecord

import scala.collection.JavaConversions._
import java.io.StringReader
import java.io.Reader

class CommonsCSVParser extends Parser {

  override def parse(inputFile: URI, schema: Schema): Iterable[Record] = {
    parse(new FileReader(new File(inputFile)), schema)
  }

  protected def parse(reader: Reader, schema: Schema): Iterable[Record] = {
    this.schema = schema

    var format = CSVFormat.EXCEL
    if (schema != null && schema.hasHeader) {
      format = format.withFirstRecordAsHeader()
    }
    var parser = format.parse(reader)

    var iterator = parser.iterator()

    if (schema == null) {
      // Fetch a record to guess schema name
      var firstrec = iterator.next()
      guessedHeader = firstrec.iterator().toArray
    }
    iterator.map(new CSVRecordWrapper(_)).toIterable
  }

  var guessedHeader: Array[String] = null;

  override def guessHeaderName: Array[String] = guessedHeader
}

class CSVRecordWrapper(inner: CSVRecord) extends Record {
  var innerRecord = inner;

  def apply(idx: Int): String = {
    inner.get(idx)
  }
  def length(): Int = {
    inner.size()
  }
  override def toString(): String = {
    inner.toString()
  }
  def iterator(): Iterator[String] = {
    inner.iterator()
  }
}