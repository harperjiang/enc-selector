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

package edu.uchicago.cs.encsel.persist.jpa

import edu.uchicago.cs.encsel.column.Column
import edu.uchicago.cs.encsel.persist.Persistence
import org.eclipse.persistence.queries.ReadAllQuery

import scala.collection.JavaConversions._

class JPAPersistence extends Persistence {

  def save(datalist: Iterable[Column]) = {
    JPAPersistence.em.getTransaction.begin()
    datalist.map(ColumnWrapper.fromColumn(_)).foreach { JPAPersistence.em.persist(_) }
    JPAPersistence.em.getTransaction.commit()
  }

  def load(): Iterable[Column] = {
    var query = JPAPersistence.em.createQuery("SELECT c FROM Column c", classOf[ColumnWrapper])
    query.getResultList.map(_.toColumn).toIterable
  }

  def clean() = {
    JPAPersistence.em.getTransaction.begin()
    var query = JPAPersistence.em.createQuery("DELETE FROM Column c", classOf[ColumnWrapper])
    query.executeUpdate()
    JPAPersistence.em.getTransaction.commit()
  }
}

object JPAPersistence {
  var em = javax.persistence.Persistence.createEntityManagerFactory("enc-selector").createEntityManager()
}