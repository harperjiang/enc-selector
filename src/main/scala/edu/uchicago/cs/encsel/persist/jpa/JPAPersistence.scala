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

import scala.collection.JavaConversions.asScalaBuffer

import edu.uchicago.cs.encsel.column.Column
import edu.uchicago.cs.encsel.persist.Persistence
import org.slf4j.LoggerFactory

class JPAPersistence extends Persistence {
  var logger = LoggerFactory.getLogger(getClass)

  def save(datalist: Traversable[Column]) = {
    var em = JPAPersistence.emf.createEntityManager()
    em.getTransaction.begin()
    try {
      datalist.map(ColumnWrapper.fromColumn(_)).foreach { data =>
        {
          data.id match {
            case 0 => em.persist(data)
            case _ => em.merge(data)
          }
        }
      }
      em.getTransaction.commit()
    } catch {
      case e: Exception => {
        logger.warn("Exception while saving data", e)
        if (em.getTransaction.isActive())
          em.getTransaction.rollback()
        throw new RuntimeException(e)
      }
    }
    em.close()
  }

  def load(): Iterator[Column] = {
    var em = JPAPersistence.emf.createEntityManager()
    var query = em.createQuery("SELECT c FROM Column c", classOf[ColumnWrapper])
    var res = query.getResultList.map(_.asInstanceOf[Column]).toIterator
    em.close
    res
  }

  def clean() = {
    var em = JPAPersistence.emf.createEntityManager()
    em.getTransaction.begin()
    try {
      var query = em.createQuery("DELETE FROM Column c", classOf[ColumnWrapper])
      query.executeUpdate()
      em.getTransaction.commit()
    } catch {
      case e: Exception => {
        em.getTransaction.rollback()
        throw new RuntimeException(e)
      }
    }
    em.close
  }
}

object JPAPersistence {
  var emf = javax.persistence.Persistence.createEntityManagerFactory("enc-selector")
}