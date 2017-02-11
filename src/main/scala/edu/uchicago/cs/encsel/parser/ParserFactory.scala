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

import edu.uchicago.cs.encsel.column.tsv.TSVColumnReader
import edu.uchicago.cs.encsel.column.csv.CSVColumnReader2
import edu.uchicago.cs.encsel.column.json.JsonColumnReader
import edu.uchicago.cs.encsel.column.excel.XLSXColumnReader
import java.net.URI
import edu.uchicago.cs.encsel.column.ColumnReader
import edu.uchicago.cs.encsel.parser.csv.CommonsCSVParser
import edu.uchicago.cs.encsel.parser.tsv.TSVParser
import edu.uchicago.cs.encsel.parser.json.LineJsonParser
import edu.uchicago.cs.encsel.parser.excel.XLSXParser
import edu.uchicago.cs.encsel.parser.col.ColParser

object ParserFactory {

  def getParser(source: URI): Parser = {
    source.getScheme match {
      case "file" => {
        source.getPath match {
          case x if x.toLowerCase().endsWith("csv") => {
            new CommonsCSVParser
          }
          case x if x.toLowerCase().endsWith("tsv") => {
            new TSVParser
          }
          case x if x.toLowerCase().endsWith("json") => {
            new LineJsonParser
          }
          case x if x.toLowerCase().endsWith("xlsx") => {
            new XLSXParser
          }
          case x if x.toLowerCase().endsWith("tmp") => {
            new ColParser
          }
          case _ =>
            return null
        }
      }
      case _ =>
        return null
    }
  }
}