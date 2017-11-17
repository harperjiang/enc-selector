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

package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.dataset.parquet.converter.RowConverter
import edu.uchicago.cs.encsel.query.ColumnPredicate
import edu.uchicago.cs.encsel.query.tpch.HorizontalScan.args
import org.apache.parquet.VersionParser.ParsedVersion
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.hadoop.metadata.BlockMetaData
import scala.collection.JavaConversions._
/**
  * Look for a value with selectivity 5%, 10%, 25%, 50%, 75% and 90%
  */
object Selectivity extends App {

  val schema = TPCHSchema.lineitemSchema
  //  val inputFolder = "/home/harper/TPCH/"
  val inputFolder = args(0)
  val suffix = ".parquet"
  val file = new File("%s%s%s".format(inputFolder, schema.getName, suffix)).toURI

  val recorder = new RowConverter(schema);

  val predicate = new ColumnPredicate[Double]((value: Double) => value < 5000)

  val start = System.currentTimeMillis()

  ParquetReaderHelper.read(file, new ReaderProcessor() {
    override def processRowGroup(version: ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore): Unit = {
      recorder.getRecords.clear()
      val cols = schema.getColumns

      val readers = cols.zipWithIndex.map(col => {
        new ColumnReaderImpl(col._1, rowGroup.getPageReader(col._1), recorder.getConverter(col._2).asPrimitiveConverter(), version)
      })

      // Predicate on column
      var counter = 0;

      val priceReader = readers(5)
      predicate.setColumn(readers(5))

      while (counter < meta.getRowCount) {
        if (predicate.test()) {
          recorder.start()
          readers.filter(_ != priceReader).foreach(reader => {
            reader.writeCurrentValueToConverter()
            reader.consume()
          })
          recorder.end()
        } else {
          readers.filter(_ != priceReader).foreach(reader => {
            reader.skip()
            reader.consume()
          })
        }
        priceReader.consume()
        counter += 1
      }
    }
  })

  val consumed = System.currentTimeMillis() - start
  println(consumed)
}
