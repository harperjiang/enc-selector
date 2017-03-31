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

package edu.uchicago.cs.encsel.ptnmining

import edu.uchicago.cs.encsel.ptnmining.parser.Token

import scala.collection.mutable.ArrayBuffer

/**
  * Created by harper on 3/31/17.
  */
class PatternMatcher {

  def matchon(ptn: Pattern, ts: Seq[Token]): Option[Data] = {
    val root = build(ptn, ts, 0)
    // Look for the first leaf that consumes all tokens
    val validpath = find(root)
    // Construct data from the path
    None
  }

  def build(ptn: Pattern, ts: Seq[Token], start: Int): MatchNode = {
    var matched = 0
    val children = new ArrayBuffer[MatchNode]
    ptn match {
      case token: PToken => {

        matched += 1
      }
      case any: PAny => {

        matched += 1
      }
      case PEmpty => {}
      case seq: PSeq => {

      }
      case union: PUnion => {
        union.content.map(item => {
        })
      }
    }
    new MatchNode(start, matched, children)
  }

  def find(node:MatchNode):Seq[MatchNode] = null
}

class MatchNode(s: Int, m: Int, c: Seq[MatchNode]) {
  val start = s
  val children: Seq[MatchNode] = c
}
