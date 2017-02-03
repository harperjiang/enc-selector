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
package edu.uchicago.cs.encsel.datacol

import java.io.File

import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

import scala.collection.JavaConversions._

import org.slf4j.LoggerFactory

import edu.uchicago.cs.encsel.colread.ColumnReader
import edu.uchicago.cs.encsel.colread.ColumnReaderFactory
import edu.uchicago.cs.encsel.colread.Schema
import edu.uchicago.cs.encsel.datacol.persist.FilePersistence
import edu.uchicago.cs.encsel.feature.Features
import edu.uchicago.cs.encsel.model.Column
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import edu.uchicago.cs.encsel.Config
import java.util.concurrent.Callable

class DataCollector {

  var persistence = Persistence.get
  var logger = LoggerFactory.getLogger(this.getClass)
  var threadPool = Executors.newFixedThreadPool(Config.collectorThreadCount)

  private def scanFunction: (Path => Iterable[Path]) = (p: Path) => {
    p match {
      case nofile if !Files.exists(nofile) => { Iterable[Path]() }
      case dir if Files.isDirectory(dir) => {
        Files.list(dir).iterator().toIterable.flatMap { scanFunction(_) }
      }
      case _ => { Iterable(p) }
    }
  }

  def scan(source: URI): Unit = {
    var target = Paths.get(source)
    var tasks = List(target).flatMap(scanFunction(_)).map {
      f => new Callable[Unit] { def call: Unit = { collect(f.toUri()) } }
    }
    threadPool.invokeAll(tasks)
  }

  def collect(source: URI): Unit = {
    try {
      var path = Paths.get(source)
      if (Files.isDirectory(path)) {
        logger.warn("Running on Directory is undefined")
        return
      }
      if (logger.isDebugEnabled())
        logger.debug("Scanning " + source.toString())

      if (isDone(source)) {
        if (logger.isDebugEnabled())
          logger.debug("Scanned mark found, skip")
        return
      }

      var colreader: ColumnReader = ColumnReaderFactory.getColumnReader(source)
      if (colreader == null) {
        if (logger.isDebugEnabled())
          logger.debug("No available reader found, skip")
        return
      }
      val defaultSchema = getSchema(source)
      if (null == defaultSchema) {
        if (logger.isDebugEnabled())
          logger.debug("Schema not found, skip")
        return
      }
      val columns = colreader.readColumn(source, defaultSchema)

      columns.foreach(extractFeature(_))

      persistence.save(columns)

      markDone(source)
      if (logger.isDebugEnabled())
        logger.debug("Scanned " + source.toString())

    } catch {
      case e: Exception => {
        logger.error("Exception while scanning " + source.toString, e)
      }
    }
  }

  protected def isDone(file: URI): Boolean = {
    return Files.exists(Paths.get(new URI("%s.done".format(file.toString()))))
  }

  protected def markDone(file: URI) = {
    Files.createFile(Paths.get(new URI("%s.done".format(file.toString()))))
  }

  private def extractFeature(col: Column): Unit = {
    try {
      col.features = Features.extract(col)
    } catch {
      case e: Exception => {
        logger.warn("Exception while processing column:%s@%s".format(col.colName, col.origin), e)
      }
    }
  }

  protected def getSchema(source: URI): Schema = {
    // file_name + .schema
    var schemaUri = new URI(source.getScheme, source.getHost,
      "%s.schema".format(source.getPath), null)
    if (new File(schemaUri).exists) {
      return Schema.fromParquetFile(schemaUri)
    }
    // file_name.abc => file_name.schema
    schemaUri = new URI(source.getScheme, source.getHost,
      source.getPath.replaceAll("\\.[\\d\\w]+$", ".schema"), null)
    if (new File(schemaUri).exists) {
      return Schema.fromParquetFile(schemaUri)
    }
    // file_name starts with schema
    var path = Paths.get(source)
    var pathname = path.getFileName.toString
    var schemas = Files.list(path.getParent).iterator().filter {
      p =>
        {
          var pname = p.getFileName.toString
          pname.endsWith(".schema") && pathname.contains(pname.replace(".schema", ""))
        }
    }
    if (!schemas.isEmpty) {
      schemaUri = schemas.next().toUri()
      return Schema.fromParquetFile(schemaUri)
    }
    return null

  }

}
