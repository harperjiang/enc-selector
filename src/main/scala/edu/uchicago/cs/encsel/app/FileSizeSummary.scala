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

import edu.uchicago.cs.encsel.persist.Persistence
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.encsel.feature.EncFileSize
import scala.collection.mutable.ArrayBuffer

import scala.collection.JavaConversions._

object FileSizeSummary extends App {

  var columns = Persistence.get.load()

  var intres = new ArrayBuffer[String]()
  var strres = new ArrayBuffer[String]()
  columns.foreach { col =>
    {
      var res = "%s,%s".format(col.colName,
        col.features.filter { _.featureType.equals("EncFileSize") }
          .map { f => (f.name, f.value) }.toList.sorted.map(p => p._2.toInt.toString()).mkString(","))
      col.dataType match {
        case DataType.INTEGER => {
          intres += res
        }
        case DataType.STRING => {
          strres += res
        }
        case _ => {}
      }
    }
  }
  System.out.println("Integer Records")
  System.out.println(intres.mkString("\n"))
  System.out.println("String Records")
  System.out.println(strres.mkString("\n"))
}