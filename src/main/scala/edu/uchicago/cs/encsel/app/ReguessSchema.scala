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

import edu.uchicago.cs.encsel.dataset.persist.Persistence
import edu.uchicago.cs.encsel.dataset.schema.SchemaGuesser
import org.slf4j.LoggerFactory

object ReguessSchema extends App {
  val cols = Persistence.get.load()
  val schemaGuesser = new SchemaGuesser()
  val logger = LoggerFactory.getLogger(getClass)
  cols.foreach { col =>
    {
      val schema = schemaGuesser.guessSchema(col.colFile)
      if (col.dataType != schema.columns(0)._1) {
        logger.warn("Unmatched data type found, %s<->%s in %s:%s".format(col.dataType, schema.columns(0)._1, col.origin, col.colName))
      }
    }
  }
}