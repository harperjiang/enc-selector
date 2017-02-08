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

import javax.persistence.Entity
import javax.persistence.Table
import edu.uchicago.cs.encsel.feature.Feature
import javax.persistence.Embeddable
import javax.persistence.Column

@Embeddable
class FeatureWrapper {

  @Column(name = "type")
  var featureType: String = null

  @Column(name = "name")
  var name: String = null

  @Column(name = "value")
  var value: Double = -1

  def toFeature: Feature = {
    new Feature(featureType, name, value)
  }
}

object FeatureWrapper {
  def fromFeature(feature: Feature): FeatureWrapper = {
    var wrapper = new FeatureWrapper
    wrapper.featureType = feature.featureType
    wrapper.name = feature.name
    wrapper.value = feature.value
    wrapper
  }
}