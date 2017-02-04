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
package edu.uchicago.cs.encsel.parser.excel

import java.io.File
import java.net.URI

import scala.collection.JavaConversions.asScalaIterator

import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import edu.uchicago.cs.encsel.parser.Parser
import edu.uchicago.cs.encsel.parser.Record
import edu.uchicago.cs.encsel.schema.Schema

class XLSXParser extends Parser {

  override def parse(inputFile: URI, schema: Schema): Iterable[Record] = {
    this.schema = schema

    var workbook = new XSSFWorkbook(new File(inputFile))

    // By default only scan the first sheet
    var sheet = workbook.getSheetAt(0)

    var iterator = sheet.rowIterator()

    if (schema == null) {
      // Fetch a record to guess schema name
      var firstrec = iterator.next()
      guessedHeader = firstrec.iterator().map(c =>
        XSSFRowRecord.content(c.asInstanceOf[XSSFCell])).toArray
    }

    iterator.map { row => new XSSFRowRecord(row.asInstanceOf[XSSFRow]) }.toIterable
  }

  var guessedHeader: Array[String] = null;

  override def guessHeaderName: Array[String] = guessedHeader

  override def parse(inputString: String, schema: Schema): Iterable[Record] = {
    throw new UnsupportedOperationException("Not supported")
  }
}

class XSSFRowRecord(row: XSSFRow) extends Record {
  var inner = row

  def apply(idx: Int): String = {
    XSSFRowRecord.content(inner.getCell(idx))
  }

  def length(): Int = {
    inner.getLastCellNum
  }

  override def toString(): String = {
    row.toString()
  }

  def iterator(): Iterator[String] = {
    inner.cellIterator().map(c => XSSFRowRecord.content(c.asInstanceOf[XSSFCell]))
  }
}

object XSSFRowRecord {
  var formatter = new DataFormatter(true)

  def content(cell: XSSFCell): String = {
    if (null == cell)
      return ""
    return formatter.formatCellValue(cell)
  }
}