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

import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.dataset.persist.jpa.ColumnWrapper
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

object Features {
  val logger = LoggerFactory.getLogger(getClass)
  val extractors = new ArrayBuffer[FeatureExtractor]()

  install(EncFileSize)
  install(Sparsity)
  install(Entropy)
  install(Length)
  install(Distinct)

  def install(fe: FeatureExtractor) = {
    extractors += fe
  }

  def extract(input: Column): Iterable[Feature] = {
    extractors.flatMap(ex => {
      try {
        val extracted = ex.extract(input)
        extracted.foreach(feature => {
          if (feature.value.isNaN)
            throw new IllegalArgumentException("NaN observed in %s:%s for column %d"
              .format(feature.featureType, feature.name, input.asInstanceOf[ColumnWrapper].id))
        })
        extracted
      } catch {
        case e: Exception => {
          logger.error("Exception while executing %s on %s:%s, skipping"
            .format(ex.getClass.getSimpleName, input.origin, input.colName), e)
          Iterable[Feature]()
        }
      }
    })
  }

  def extract(input: Column,
              filter: Iterator[String] => Iterator[String],
              prefix: String): Iterable[Feature] = {
    extractors.filter(_.supportFilter).flatMap(ex => {
      try {
        ex.extract(input, filter, prefix)
      } catch {
        case e: Exception => {
          logger.error("Exception while executing %s on %s:%s, skipping"
            .format(ex.getClass.getSimpleName, input.origin, input.colName), e)
          Iterable[Feature]()
        }
      }
    })
  }
}