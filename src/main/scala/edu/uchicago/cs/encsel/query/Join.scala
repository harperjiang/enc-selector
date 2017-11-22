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
import edu.uchicago.cs.encsel.dataset.parquet.converter.RowTempTable
import org.apache.parquet.VersionParser
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.hadoop.Footer
import org.apache.parquet.hadoop.metadata.BlockMetaData
import org.apache.parquet.schema.MessageType

import scala.collection.JavaConversions._

trait Join {

  def join(left: URI, leftSchema: MessageType, right: URI, rightSchema: MessageType, joinKey: (Int, Int));
}

class BlockNestedLoopJoin(val hash: (Any) => Long, val numBlock: Int) extends Join {

  def join(left: URI, leftSchema: MessageType, right: URI, rightSchema: MessageType, joinKey: (Int, Int)) = {

    val leftBlocks = Array()
    val rightBlocks = Array()

    val leftRecorder = new RowTempTable(leftSchema)
    val rightRecorder = new RowTempTable(rightSchema)

    ParquetReaderHelper.read(left, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {

      }

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {
        val readers = leftSchema.getColumns.map(col => new ColumnReaderImpl(col, rowGroup.getPageReader(col),
          leftRecorder.getConverter(col.getPath).asPrimitiveConverter(), version))

        //        forreaders(joinKey._1)
      }
    })

    ParquetReaderHelper.read(right, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {

      }

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
    val probeRecorder = new RowTempTable(probeSchema)

    ParquetReaderHelper.read(hashFile, new ReaderProcessor() {
      override def processFooter(footer: Footer) = {

      }

      override def processRowGroup(version: VersionParser.ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore) = {
        val readers = hashSchema.getColumns.map(col => new ColumnReaderImpl(col, rowGroup.getPageReader(col),
          hashRecorder.getConverter(col.getPath).asPrimitiveConverter(), version))

        //        forreaders(joinKey._1)
      }
    })
  }
}
