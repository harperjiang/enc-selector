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

import edu.uchicago.cs.encsel.model.DataType
import javax.persistence.AttributeConverter

@javax.persistence.Converter
class URIConverter extends AttributeConverter[URI,String] {
  def convertToDatabaseColumn(objectValue: URI): String = objectValue.asInstanceOf[URI].toString
  def convertToEntityAttribute(dataValue: String): URI = new URI(dataValue.toString)
}

@javax.persistence.Converter
class DataTypeConverter extends AttributeConverter[DataType,String] {
  def convertToDatabaseColumn(objectValue: DataType): String = objectValue.asInstanceOf[DataType].name()
  def convertToEntityAttribute(dataValue: String): DataType = DataType.valueOf(dataValue.toString)
  
}