/**
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
 */
package edu.uchicago.cs.encsel.colread.excel

import scala.collection.JavaConversions._

import edu.uchicago.cs.encsel.colread.ColumnReader
import org.apache.commons.csv.CSVRecord
import edu.uchicago.cs.encsel.model.Column
import java.net.URI
import edu.uchicago.cs.encsel.Config
import edu.uchicago.cs.encsel.colread.Schema
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter

class XLSXColumnReader extends ColumnReader {

  def readColumn(source: URI, schema: Schema): Iterable[Column] = {

    fireStart(source)

    var tempFolder = allocTempFolder(source)
    var colWithWriter = schema.columns.zipWithIndex.map(d => {
      var col = new Column(source, d._2, d._1._2, d._1._1)
      col.colFile = allocFileForCol(tempFolder, d._1._2, d._2)
      var writer = new PrintWriter(new FileOutputStream(new File(col.colFile)))
      (col, writer)
    }).toArray

    var workbook = new XSSFWorkbook(new File(source))
    // By default only scan the first sheet
    var sheet = workbook.getSheetAt(0)
    var iterator = sheet.rowIterator()
    if (schema.hasHeader) {
      iterator.next()
    }
    iterator.foreach { record =>
      {
        fireReadRecord(source)
        var row = record.asInstanceOf[XSSFRow]
        if (!validate(row, schema)) {
          fireFailRecord(source)
          logger.warn("Malformated record in %s at %d found, skipping: %s"
            .format(source.toString, record.getRowNum, record.toString))
        } else {
          colWithWriter.foreach(pair => {
            var col = pair._1
            var writer = pair._2
            writer.println(content(row.getCell(col.colIndex)))
          })
        }
      }
    }
    colWithWriter.foreach(t => { t._2.close })
    fireDone(source)
    return colWithWriter.map(_._1)
  }

  def validate(record: XSSFRow, schema: Schema): Boolean = {
    if (!Config.columnReaderEnableCheck)
      return true

    schema.columns.zipWithIndex.foreach(col => {
      var cell = record.getCell(col._2)
      var datatype = col._1._1
      if (!datatype.check(content(cell)))
        return false
    })
    return true
  }

  var formatter = new DataFormatter(true)

  def content(cell: XSSFCell): String = {
    if (null == cell)
      return ""
    return formatter.formatCellValue(cell)
  }
}