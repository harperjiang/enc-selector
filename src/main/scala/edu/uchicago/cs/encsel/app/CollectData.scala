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
package edu.uchicago.cs.encsel.app;

import java.io.File

import java.util.concurrent.Executors
import java.nio.file.Paths
import edu.uchicago.cs.encsel.column.ColumnReaderFactory
import java.util.concurrent.Callable
import edu.uchicago.cs.encsel.column.Column
import java.net.URI
import edu.uchicago.cs.encsel.persist.Persistence
import java.nio.file.Path
import java.nio.file.Files
import edu.uchicago.cs.encsel.Config
import edu.uchicago.cs.encsel.column.ColumnReader
import edu.uchicago.cs.encsel.schema.Schema
import org.slf4j.LoggerFactory
import edu.uchicago.cs.encsel.feature.Features

import scala.collection.JavaConversions._
import edu.uchicago.cs.encsel.util.FileUtils

object CollectData extends App {
  var f = new File(args(0))
  //  var f = new File("/home/harper/dataset")
  new DataCollector().scan(f.toURI())
}

class DataCollector {

  var persistence = Persistence.get
  var logger = LoggerFactory.getLogger(this.getClass)
  var threadPool = Executors.newFixedThreadPool(Config.collectorThreadCount)

  def scan(source: URI): Unit = {
    var target = Paths.get(source)
    var tasks = scala.collection.immutable.List(target).flatMap(FileUtils.scanFunction(_)).map { p =>
      {
        new Callable[Unit] { def call: Unit = { collect(p.toUri()) } }
      }
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

      var columner: ColumnReader = ColumnReaderFactory.getColumnReader(source)
      if (columner == null) {
        if (logger.isDebugEnabled())
          logger.debug("No available reader found, skip")
        return
      }
      val defaultSchema = Schema.getSchema(source)
      if (null == defaultSchema) {
        if (logger.isDebugEnabled())
          logger.debug("Schema not found, skip")
        return
      }
      val columns = columner.readColumn(source, defaultSchema)

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
    return FileUtils.isDone(file, "done")
  }

  protected def markDone(file: URI) = {
    FileUtils.markDone(file, "done")
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

}