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
package edu.uchicago.cs.encsel.dataset.feature

import java.io.File

import scala.Iterable
import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.model._
import edu.uchicago.cs.encsel.dataset.parquet.ParquetWriterHelper

object EncFileSize extends FeatureExtractor {

  def featureType = "EncFileSize"

  def supportFilter: Boolean = false

  def extract(col: Column,
              filter: Iterator[String] => Iterator[String],
              prefix: String): Iterable[Feature] = {
    // Ignore filter
    val fType = "%s%s".format(prefix, featureType)
    col.dataType match {
      case DataType.STRING => {
        StringEncoding.values().map { e => {
          val f = ParquetWriterHelper.singleColumnString(col.colFile, e)
          new Feature(fType, "%s_file_size".format(e.name()), new File(f).length)
        }
        }
      }
      case DataType.LONG => {
        LongEncoding.values().map { e => {
          val f = ParquetWriterHelper.singleColumnLong(col.colFile, e)
          new Feature(fType, "%s_file_size".format(e.name()), new File(f).length)
        }
        }
      }
      case DataType.INTEGER => {
        IntEncoding.values().map { e => {
          val f = ParquetWriterHelper.singleColumnInt(col.colFile, e)
          new Feature(fType, "%s_file_size".format(e.name()), new File(f).length)
        }
        }
      }
      case DataType.FLOAT => {
        FloatEncoding.values().map { e => {
          val f = ParquetWriterHelper.singleColumnFloat(col.colFile, e)
          new Feature(fType, "%s_file_size".format(e.name()), new File(f).length)
        }
        }
      }
      case DataType.DOUBLE => {
        FloatEncoding.values().map { e => {
          val f = ParquetWriterHelper.singleColumnDouble(col.colFile, e)
          new Feature(fType, "%s_file_size".format(e.name()), new File(f).length)
        }
        }
      }
      case DataType.BOOLEAN => Iterable[Feature]() // Ignore BOOLEAN type
    }
  }

}