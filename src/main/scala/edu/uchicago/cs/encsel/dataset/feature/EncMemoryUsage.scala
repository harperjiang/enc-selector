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
import java.lang.management.{ManagementFactory, MemoryMXBean}
import javax.management.remote.{JMXConnectorFactory, JMXServiceURL}
import javax.management.{JMX, ObjectName}
import javax.persistence.NoResultException

import scala.collection.JavaConversions._
import com.sun.tools.attach.VirtualMachine
import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.dataset.parquet.ParquetWriterHelper
import edu.uchicago.cs.encsel.dataset.persist.jpa.{ColumnWrapper, JPAPersistence}
import edu.uchicago.cs.encsel.model._
import edu.uchicago.cs.encsel.tool.mem.JMXMemoryMonitor
import org.slf4j.LoggerFactory

object EncMemoryUsage extends FeatureExtractor {
  val logger = LoggerFactory.getLogger(getClass)

  def featureType = "EncMemoryUsage"

  def supportFilter: Boolean = false

  def extract(col: Column, prefix: String): Iterable[Feature] = {
    // Ignore filter
    val fType = "%s%s".format(prefix, featureType)
    col.dataType match {
      case DataType.STRING => {
        StringEncoding.values().map { e => {
          try {
            new Feature(fType, "%s_maxheap".format(e.name()), executeAndMonitor(col, e.name()))
          } catch {
            case ile: IllegalArgumentException => {
              // Unsupported Encoding, ignore
              logger.warn("Exception when applying Encoding", ile.getMessage)
              null
            }
          }
        }
        }.filter(_ != null)
      }
      case DataType.LONG => {
        LongEncoding.values().map { e => {
          try {
            new Feature(fType, "%s_maxheap".format(e.name()), executeAndMonitor(col, e.name()))
          } catch {
            case ile: IllegalArgumentException => {
              logger.warn("Exception when applying Encoding", ile.getMessage)
              null
            }
          }
        }
        }.filter(_ != null)
      }
      case DataType.INTEGER => {
        IntEncoding.values().map { e => {
          try {
            new Feature(fType, "%s_maxheap".format(e.name()), executeAndMonitor(col, e.name()))
          } catch {
            case ile: IllegalArgumentException => {
              logger.warn("Exception when applying Encoding", ile.getMessage)
              null
            }
          }
        }
        }.filter(_ != null)
      }
      case DataType.FLOAT => {
        FloatEncoding.values().map { e => {
          try {
            new Feature(fType, "%s_maxheap".format(e.name()), executeAndMonitor(col, e.name()))
          } catch {
            case ile: IllegalArgumentException => {
              logger.warn("Exception when applying Encoding", ile.getMessage)
              null
            }
          }
        }
        }.filter(_ != null)
      }
      case DataType.DOUBLE => {
        FloatEncoding.values().map { e => {
          try {
            new Feature(fType, "%s_maxheap".format(e.name()), executeAndMonitor(col, e.name()))
          } catch {
            case ile: IllegalArgumentException => {
              logger.warn("Exception when applying Encoding", ile.getMessage)
              null
            }
          }
        }
        }.filter(_ != null)
      }
      case DataType.BOOLEAN => Iterable[Feature]() // Ignore BOOLEAN type
    }
  }

  /**
    * Get the memory usage of encoding this column with given encoding
    *
    * @param col
    * @param encoding
    * @return
    */
  def executeAndMonitor(col: Column, encoding: String): Long = {
    // Create Process
    val pb = new ProcessBuilder("/usr/bin/java",
      "-Xmx2G",
      "-cp",
      "/home/harper/IdeaProjects/enc-selector/target/enc-selector-0.0.1-SNAPSHOT-jar-with-dependencies.jar:/usr/lib/jvm/jdk1.8.0/lib/tools.jar",
       "edu.uchicago.cs.encsel.dataset.feature.EncMemoryUsageProcess",
      col.asInstanceOf[ColumnWrapper].id.toString, encoding)
    val process = pb.start()

    val pidfield = process.getClass.getDeclaredField("pid")
    pidfield.setAccessible(true)
    val pid = pidfield.get(process).toString

    // Attach VM and obtain MemoryMXBean
    val vm = VirtualMachine.attach(pid)

    val jmxMemoryMonitor = new JMXMemoryMonitor(vm)

    var maxMemory = 0l

    while (process.isAlive) {
      Thread.sleep(100l);
      val memoryUsage = jmxMemoryMonitor.getHeapMemoryUsage
      memoryUsage match {
        case Some(mu) => {
          maxMemory = Math.max(mu.getUsed, maxMemory)
        }
        case None => {}
      }
      println(maxMemory)
    }

    return maxMemory
  }
}

object EncMemoryUsageRun extends App {
  val colId = args(0).toInt
  val col = new ColumnWrapper
  col.id = colId
  val maxMemory = EncMemoryUsage.executeAndMonitor(col, args(1))
  println(maxMemory)
}

/**
  * This is the main entry to load a column from database and
  * encode it using one encoding. Parent application will monitor the
  * memory usage using JMX and record the result
  */
object EncMemoryUsageProcess extends App {

  val colId = args(0)
  val encoding = args(1)

  val emf = JPAPersistence.emf
  val em = emf.createEntityManager()

  try {
    val col = em.createQuery("select c from Column c where c.id = :id", classOf[ColumnWrapper])
      .setParameter("id", colId.toInt).getSingleResult
    col.dataType match {
      case DataType.INTEGER => {
        val e = IntEncoding.valueOf(encoding)
        ParquetWriterHelper.singleColumnInt(col.colFile, e)
      }
      case DataType.LONG => {
        val e = LongEncoding.valueOf(encoding)
        ParquetWriterHelper.singleColumnLong(col.colFile, e)
      }
      case DataType.STRING => {
        val e = StringEncoding.valueOf(encoding)
        ParquetWriterHelper.singleColumnString(col.colFile, e)
      }
      case DataType.DOUBLE => {
        val e = FloatEncoding.valueOf(encoding)
        ParquetWriterHelper.singleColumnDouble(col.colFile, e)
      }
      case DataType.FLOAT => {
        val e = FloatEncoding.valueOf(encoding)
        ParquetWriterHelper.singleColumnFloat(col.colFile, e)
      }
      case _ => {

      }
    }
  }
  catch {
    case e: NoResultException => {

    }
  }
}