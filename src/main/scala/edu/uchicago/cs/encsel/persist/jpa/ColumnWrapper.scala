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

import java.net.URI

import scala.collection.JavaConversions._

import edu.uchicago.cs.encsel.column.Column
import edu.uchicago.cs.encsel.model.DataType
import javax.persistence.CollectionTable
import javax.persistence.Convert
import javax.persistence.ElementCollection
import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.GeneratedValue
import javax.persistence.JoinColumn
import javax.persistence.GenerationType
import scala.collection.mutable.ListBuffer
import javax.persistence.TableGenerator

@Entity(name = "Column")
@Table(name = "col_data")
class ColumnWrapper {

  @Id
  @TableGenerator(name = "TABLE_GEN", table = "seq_table", pkColumnName = "name",
    valueColumnName = "counter", pkColumnValue = "COL_DATA")
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
  @javax.persistence.Column(name = "id")
  var id: Int = 0

  @javax.persistence.Column(name = "origin_uri")
  @Convert(converter = classOf[URIConverter])
  var origin: URI = null

  @javax.persistence.Column(name = "idx")
  var colIndex: Int = -1

  @javax.persistence.Column(name = "name")
  var colName: String = null

  @javax.persistence.Column(name = "file_uri")
  @Convert(converter = classOf[URIConverter])
  var colFile: URI = null

  @javax.persistence.Column(name = "data_type")
  @Convert(converter = classOf[DataTypeConverter])
  var dataType: DataType = null

  @ElementCollection
  @CollectionTable(name = "feature", joinColumns = Array(new JoinColumn(name = "col_id")))
  var features: java.util.List[FeatureWrapper] = null

  def toColumn(): Column = {
    var col = new Column(origin, colIndex, colName, dataType)
    col.colFile = this.colFile
    col.features = this.features.map(_.toFeature).toArray.toIterable
    col
  }
}

object ColumnWrapper {
  def fromColumn(col: Column): ColumnWrapper = {
    var wrapper = new ColumnWrapper
    wrapper.colFile = col.colFile
    wrapper.colName = col.colName
    wrapper.colIndex = col.colIndex
    wrapper.dataType = col.dataType
    wrapper.origin = col.origin

    wrapper.features = ListBuffer(col.features.map { FeatureWrapper.fromFeature(_) }.toSeq: _*)

    wrapper
  }
}

