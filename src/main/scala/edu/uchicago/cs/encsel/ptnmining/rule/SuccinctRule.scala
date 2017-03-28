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
 */

package edu.uchicago.cs.encsel.ptnmining.rule

import edu.uchicago.cs.encsel.ptnmining.{PSeq, PToken, PUnion, Pattern}

/**
  * Remove unnecessary seq or union structure
  */
class SuccinctRule extends RewriteRule {

  def rewrite(ptn: Pattern): Pattern = {
    // First look for union and extract common patterns from it
    modify(ptn, p => p.isInstanceOf[PUnion] || p.isInstanceOf[PSeq], update).get
  }

  def update(up: Pattern): Pattern = {
    up match {
      case seq: PSeq if seq.content.length == 1 => {
        happen
        seq.content(0)
      }
      case union: PUnion if union.content.length == 1 => {
        happen
        union.content(0)
      }
      case _ => up
    }
  }
}
