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
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.encsel.query.operator

import java.net.URI

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.dataset.parquet.converter.{ColumnTempTable, PipePrimitiveConverter, Row, RowTempTable}
import edu.uchicago.cs.encsel.query.util.{DataUtils, SchemaUtils}
import edu.uchicago.cs.encsel.query.{Bitmap}
import org.apache.parquet.VersionParser
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.hadoop.Footer
import org.apache.parquet.hadoop.metadata.BlockMetaData
import org.apache.parquet.schema.MessageType

import scala.collection.mutable
import scala.collection.JavaConversions._

class HashJoin extends Join {
  def join(hashFile: URI, hashSchema: MessageType, probeFile: URI, probeSchema: MessageType, joinKey: (Int, Int),
           hashProject: Array[Int], probeProject: Array[Int]) = {

    val hashProjectSchema = SchemaUtils.project(hashSchema, hashProject)
    val hashRecorder = new RowTempTable(hashProjectSchema)

    val probeProjectSchema = SchemaUtils.project(probeSchema, probeProject)

    val hashtable = new mutable.HashMap[Any, Row]()

    val joinedSchema = SchemaUtils.join(hashSchema, probeSchema, hashProject, probeProject)
    val outputRecorder = new ColumnTempTable(joinedSchema)

    // Build Hash Table
    ParquetReaderHelper.read(hashFile, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {}

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {
        val hashRowReaders = hashProjectSchema.getColumns.map(col => new ColumnReaderImpl(col, rowGroup.getPageReader(col),
          hashRecorder.getConverter(col.getPath).asPrimitiveConverter(), version))

        val hashKeyCol = hashSchema.getColumns()(joinKey._1)
        val hashKeyReader = new ColumnReaderImpl(hashKeyCol, rowGroup.getPageReader(hashKeyCol),
          new PipePrimitiveConverter(hashSchema.getType(joinKey._1).asPrimitiveType()), version)
        // Build hash table
        for (i <- 0L until rowGroup.getRowCount) {
          val hashKey = DataUtils.readValue(hashKeyReader)

          hashRecorder.start()
          hashRowReaders.foreach(reader => {
            reader.writeCurrentValueToConverter()
            reader.consume()
          })
          hashRecorder.end()

          hashtable.put(hashKey, hashRecorder.getCurrentRecord)
        }
      }
    })

    // Probe Hash Table
    ParquetReaderHelper.read(probeFile, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {}

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {

        val hashKeyCol = probeSchema.getColumns()(joinKey._2)
        val hashKeyReader = new ColumnReaderImpl(hashKeyCol, rowGroup.getPageReader(hashKeyCol),
          new PipePrimitiveConverter(probeSchema.getType(joinKey._2).asPrimitiveType()), version)
        // Build bitmap
        val bitmap = new Bitmap(rowGroup.getRowCount)

        for (i <- 0L until rowGroup.getRowCount) {
          val hashKey = DataUtils.readValue(hashKeyReader)
          hashtable.get(hashKey) match {
            case Some(row) => {
              // Record match in bitmap
              bitmap.set(i, true)
              // Write remaining field to output
              for (j <- 0 until hashProjectSchema.getColumns.size) {
                DataUtils.writeValue(outputRecorder.getConverter(j).asPrimitiveConverter(), row.getData()(j))
              }
            }
            case None => {}
          }
        }

        val probeReaders = probeProjectSchema.getColumns.zipWithIndex
          .map(col => new ColumnReaderImpl(col._1, rowGroup.getPageReader(col._1),
            outputRecorder.getConverter(hashProject.length + col._2).asPrimitiveConverter(), version))

        // Based on bitmap, write remaining columns
        for (j <- 0 until probeProjectSchema.getColumns.size) {
          val source = probeReaders(j)
          for (i <- 0L until rowGroup.getRowCount) {
            // Write probe
            bitmap.test(i) match {
              case true => {
                source.writeCurrentValueToConverter()
              }
              case false => {
                source.skip()
              }
            }
            source.consume()
          }
        }
      }
    })
  }
}
