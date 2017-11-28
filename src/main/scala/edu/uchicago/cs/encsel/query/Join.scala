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

package edu.uchicago.cs.encsel.query

import java.net.URI

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.dataset.parquet.converter.{ColumnTempTable, Row, RowTempTable}
import org.apache.parquet.VersionParser
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.hadoop.Footer
import org.apache.parquet.hadoop.metadata.BlockMetaData
import org.apache.parquet.schema.MessageType

import scala.collection.JavaConversions._
import scala.collection.mutable

trait Join {

  def join(left: URI, leftSchema: MessageType, right: URI, rightSchema: MessageType, joinKey: (Int, Int));
}

class BlockNestedLoopJoin(val hash: (Any) => Long, val numBlock: Int) extends Join {

  def join(left: URI, leftSchema: MessageType, right: URI, rightSchema: MessageType, joinKey: (Int, Int)) = {

    val leftBlocks = new Array[ColumnTempTable](numBlock)
    val rightBlocks = new Array[ColumnTempTable](numBlock)
    for (i <- 0 until numBlock) {
      leftBlocks(i) = new ColumnTempTable(leftSchema)
      rightBlocks(i) = new ColumnTempTable(rightSchema)
    }

    val leftRecorder = new RowTempTable(leftSchema)
    val rightRecorder = new RowTempTable(rightSchema)

    ParquetReaderHelper.read(left, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {}

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {
        val readers = leftSchema.getColumns.map(col => new ColumnReaderImpl(col, rowGroup.getPageReader(col),
          leftRecorder.getConverter(col.getPath).asPrimitiveConverter(), version))

          readers(joinKey._1)
      }
    })

    ParquetReaderHelper.read(right, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {}

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {
        val readers = rightSchema.getColumns.map(col => new ColumnReaderImpl(col, rowGroup.getPageReader(col),
          rightRecorder.getConverter(col.getPath).asPrimitiveConverter(), version))
      }
    })

  }
}

class HashJoin extends Join {
  def join(hashFile: URI, hashSchema: MessageType, probeFile: URI, probeSchema: MessageType, joinKey: (Int, Int)) = {

    val hashRecorder = new RowTempTable(hashSchema)

    val hashtable = new mutable.HashMap[Any, Row]()

    // Build Hash Table
    ParquetReaderHelper.read(hashFile, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {}

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {
        val readers = hashSchema.getColumns.map(col => new ColumnReaderImpl(col, rowGroup.getPageReader(col),
          hashRecorder.getConverter(col.getPath).asPrimitiveConverter(), version))

        val hashKeyReader = readers(joinKey._1)
        // Build hash table
        for (i <- 0L until rowGroup.getRowCount) {
          val hashKey = DataUtils.readValue(hashKeyReader)

          hashRecorder.start()
          readers.foreach(reader => {
            reader.writeCurrentValueToConverter()
            reader.consume()
          })
          hashRecorder.end()

          hashtable.put(hashKey, hashRecorder.getCurrentRecord)
        }
      }
    })

    val outputRecorder = new ColumnTempTable(hashSchema, probeSchema)
    // Probe Hash Table
    ParquetReaderHelper.read(probeFile, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {}

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {
        val probeReaders = probeSchema.getColumns.map(col => new ColumnReaderImpl(col, rowGroup.getPageReader(col),
          outputRecorder.getConverter(1, col.getPath).asPrimitiveConverter(), version))

        val hashKeyReader = probeReaders(joinKey._2)
        // Build hash table
        for (i <- 0L until rowGroup.getRowCount) {
          val hashKey = DataUtils.readValue(hashKeyReader)

          hashtable.get(hashKey) match {
            case Some(row) => {
              outputRecorder.start()

              // Write hash
              for (i <- 0 until hashSchema.getColumns.size) {
                DataUtils.writeValue(outputRecorder.getConverter(Array(0, i)).asPrimitiveConverter(), row.getData()(i))
              }

              // Write probe
              probeReaders.foreach(reader => {
                reader.writeCurrentValueToConverter()
                reader.consume()
              })
              outputRecorder.end()
            }
            case None => {}
          }
        }
      }
    })
  }
}
