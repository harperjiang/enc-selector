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

package edu.uchicago.cs.encsel.ptnmining.eval

import edu.uchicago.cs.encsel.ptnmining.{PAny, Pattern}
import edu.uchicago.cs.encsel.ptnmining.parser.Token
import org.apache.commons.lang3.StringUtils

/**
  * <code>PatternEvaluator</code> evaluates a given pattern on a dataset to
  * determine its efficiency
  *
  */
object PatternEvaluator {
  def evaluate(ptn: Pattern, dataset: Seq[Seq[Token]]): Double = {

    if (StringUtils.isEmpty(ptn.getName))
      ptn.naming()

    // Pattern Size
    val sizeVisitor = new SizeVisitor
    ptn.visit(sizeVisitor)
    val ptnSize = sizeVisitor.size

    val anyNames = ptn.flatten.filter(_.isInstanceOf[PAny]).map(_.getName).toSet

    // Encoded Data Size
    val matched = dataset.map(di => (ptn.matchon(di), di))

    val encodedSize = matched.map(item => {
      val record = item._1
      val origin = item._2
      record.isDefined match {
        case true => {
          val content = record.get
          val unionSel = content.choices.values
            .map(x => Math.ceil(Math.log(x._2) / (8 * Math.log(2))).toInt).sum
          val anyLength = anyNames.map(name => {
            content.get(name) match {
              case Some(token) => token.length
              case None => 0
            }
          }).sum
          unionSel + anyLength
        }
        case false => origin.map(_.length).sum
      }
    })

    ptnSize + encodedSize.sum
  }
}
