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

package edu.uchicago.cs.encsel.classify

import edu.uchicago.cs.encsel.dataset.persist.Persistence
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.ndnn.DefaultDataset

object EncselDataset {
  val featureMap = Map(
    (DataType.STRING -> Seq(("", "")))
    , (DataType.INTEGER -> Seq(("", "")))
  )
}

/**
  * Encoding Selector Dataset. Load Column Data from Database with the given data type
  */
class EncselDataset(val dataType: DataType) extends DefaultDataset {


  override def load(): (Array[Array[Double]], Array[Double]) = {


    val pair = Persistence.get.load().map(
      column => {
        val mapping = EncselDataset.featureMap.getOrElse(dataType, Seq.empty[(String, String)])
        mapping.foreach(m => {
          val feature = column.findFeature(m._1, m._2)
        })
      }
    )

    null
  }
}
