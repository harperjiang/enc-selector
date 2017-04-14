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

package edu.uchicago.cs.encsel.app

import scala.Iterable
import edu.uchicago.cs.encsel.dataset.feature._
import edu.uchicago.cs.encsel.dataset.persist.Persistence

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import edu.uchicago.cs.encsel.dataset.column.Column
import org.slf4j.LoggerFactory
import edu.uchicago.cs.encsel.dataset.persist.jpa.{ColumnWrapper, JPAPersistence}

object AddMissingFeature extends App {

  val logger = LoggerFactory.getLogger(getClass)

  val persist = new JPAPersistence

  val em = JPAPersistence.emf.createEntityManager()
  em.getTransaction.begin()
  val query = em.createNativeQuery("SELECT c.* FROM col_data c",
    classOf[ColumnWrapper])
  query.getResultList.foreach(colnotype => {
    val column = colnotype.asInstanceOf[Column]
    Features.extractors.foreach(fe => {
      if (!column.hasFeature(fe.featureType)) {
        column.features ++= fe.extract(column)
      }
    })
  })

  em.getTransaction.commit()
}