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
package edu.uchicago.cs.encsel.schema

import java.net.URI
import edu.uchicago.cs.encsel.parser.Record
import edu.uchicago.cs.encsel.parser.ParserFactory
import edu.uchicago.cs.encsel.column.Column
import edu.uchicago.cs.encsel.model.DataType

import scala.util.Try
import org.slf4j.LoggerFactory

class SchemaGuesser {

  var logger = LoggerFactory.getLogger(getClass)

  def guessSchema(file: URI): Schema = {
    var parser = ParserFactory.getParser(file)
    if (null == parser) {
      if (logger.isDebugEnabled())
        logger.debug("No parser available for %s".format(file.toString()))
      return null
    }
    var records = parser.parse(file, null)

    var guessedHeader = parser.guessHeaderName()
    var columns = guessedHeader.map((DataType.BOOLEAN, _)).toArray

    records.foreach { record =>
      {
        for (i <- 0 until columns.length) {
          var value = record(i)
          if (value != null && value.trim().length() != 0) {
            value = value.trim()

            var expected = testType(value, columns(i)._1)
            if (expected != columns(i)._1)
              columns(i) = (expected, columns(i)._2)
          }
        }
      }
    }
    new Schema(columns, true)
  }

  protected var booleanValues = Set("0", "1", "yes", "no", "true", "false")
  protected val numberRegex = """[\-]?\d+""".r
  protected val floatRegex = """[\-]?\d+(\.\d*)?""".r

  def testType(input: String, expected: DataType): DataType = {
    expected match {
      case DataType.BOOLEAN => {
        if (booleanValues.contains(input.toLowerCase()))
          DataType.BOOLEAN
        else
          testType(input, DataType.INTEGER)
      }
      case DataType.INTEGER => {
        input match {
          case numberRegex(_*) => {
            if (input.length() >= Long.MinValue.toString.length)
              return DataType.STRING
            if (input.length() >= Int.MinValue.toString.length) {
              return testType(input, DataType.LONG)
            }
            Try({ input.toInt; DataType.INTEGER })
              .getOrElse(DataType.LONG)
          }
          case floatRegex(_*) => testType(input, DataType.DOUBLE)
          case _ => DataType.STRING
        }
      }
      case DataType.LONG => {
        input match {
          case numberRegex(_*) => {
            if (input.length() >= Long.MinValue.toString.length)
              return DataType.STRING
            Try({ input.toLong; DataType.LONG })
              .getOrElse(DataType.STRING)
          }
          case floatRegex(_*) => testType(input, DataType.DOUBLE)
          case _ => DataType.STRING
        }
      }
      case DataType.FLOAT => {
        input match {
          case floatRegex(_*) =>
            Try({ input.toFloat; DataType.FLOAT })
              .getOrElse(testType(input, DataType.DOUBLE))
          case _ => DataType.STRING
        }
      }
      case DataType.DOUBLE => {
        input match {
          case floatRegex(_*) =>
            Try({ input.toDouble; DataType.DOUBLE }).getOrElse(DataType.STRING)
          case _ => DataType.STRING
        }
      }
      case DataType.STRING => expected
    }
  }
}