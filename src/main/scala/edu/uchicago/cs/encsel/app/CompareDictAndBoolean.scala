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
package edu.uchicago.cs.encsel.app

import java.net.URI
import edu.uchicago.cs.encsel.dataset.persist.Persistence
import scala.io.Source
import java.io.File
import scala.collection.mutable.HashSet
import java.nio.file.Files
import java.nio.file.Paths
import edu.uchicago.cs.encsel.Config
import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.dataset.persist.jpa.ColumnWrapper
import scala.util.Try
import java.io.FileWriter
import java.io.BufferedWriter
import java.io.Writer
import org.apache.commons.io.output.NullWriter
import java.io.PrintWriter
import edu.uchicago.cs.encsel.dataset.parquet.ParquetWriterHelper
import edu.uchicago.cs.encsel.model.DataType
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName
import org.apache.parquet.hadoop.ParquetWriter
import edu.uchicago.cs.encsel.dataset.parquet.ParquetWriterBuilder
import org.apache.parquet.schema.Type.Repetition
import org.apache.parquet.schema.MessageType
import java.util.ArrayList
import org.apache.parquet.schema.PrimitiveType
import org.apache.hadoop.fs.Path

import scala.collection.JavaConversions._

object CompareDictAndBoolean extends App {

  val threshold = 20

  var cols = Persistence.get.load()
  cols.filter { col => { col.colName.toLowerCase().contains("category") && col.dataType == DataType.STRING && col.findFeature("Sparsity", "valid_ratio").value == 1 } }
    .foreach(col => {
      var distvals = distinct(col.colFile)
      if (distvals.size < threshold) {
        genColumn(distvals, col)
        genColumn2(distvals, col)
      }
    })

  def distinct(colFile: URI): Set[String] = {
    var values = new HashSet[String]()
    Source.fromFile(new File(colFile)).getLines().foreach(values += _.trim())
    values.toSet
  }

  def genColumn(distval: Set[String], column: Column) = {
    var folder = Files.createTempDirectory(Paths.get(Config.tempFolder),
      Try { column.asInstanceOf[ColumnWrapper].id.toString() + "_" }.getOrElse(column.colName))
    var files = distval.map(value => { (value, folder.resolve(namize(value)).toFile()) }).toMap
    var writers = files.map(kv => { (kv._1, new PrintWriter(new FileWriter(kv._2))) })

    Source.fromFile(new File(column.colFile)).getLines().foreach(line => {
      writers.foreach(writer => writer._2.println(line.trim() match {
        case writer._1 => "true"
        case _ => "false"
      }))
    })
    writers.foreach(_._2.close)

    var sumLength = files.toList.map(f => new File(ParquetWriterHelper.singleColumnBoolean(f._2.toURI())).length()).sum
    var dictLength = column.findFeature("EncFileSize", "DICT_file_size").value
    println(distval.size, sumLength, dictLength, sumLength / dictLength)
  }

  def genColumn2(distval: Set[String], column: Column) = {
    var folder = Files.createTempDirectory(Paths.get(Config.tempFolder),
      Try { column.asInstanceOf[ColumnWrapper].id.toString() + "_" }.getOrElse(column.colName))
    var output = folder.resolve(column.colName)
    var row = distval.toList
    var schema = new MessageType("record",
      row.zipWithIndex.map(i => new PrimitiveType(Repetition.OPTIONAL, PrimitiveTypeName.BOOLEAN, "value_2_%d".format(i._2))));

    var writer: ParquetWriter[java.util.List[String]] = ParquetWriterBuilder.buildDefault(new Path(output.toUri()), schema, false);

    Source.fromFile(new File(column.colFile)).getLines().foreach(line =>
      writer.write(row.map { elem => line.equals(elem).toString() }))

    writer.close();

    var sumLength = output.toFile().length
    var dictLength = column.findFeature("EncFileSize", "DICT_file_size").value
    println(distval.size, sumLength, dictLength, sumLength / dictLength)
  }

  def namize(input: String) = input.replaceAll("""[^\d\w]""", "_")
}

