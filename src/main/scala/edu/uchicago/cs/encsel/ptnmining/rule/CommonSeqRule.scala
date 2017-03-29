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

import edu.uchicago.cs.encsel.ptnmining.parser.TWord
import edu.uchicago.cs.encsel.ptnmining.{PSeq, PToken, PUnion, Pattern}

import scala.collection.mutable.ArrayBuffer

/**
  * Look for common sequence from a union and split it into smaller pieces
  *
  * Created by harper on 3/27/17.
  */
class CommonSeqRule extends RewriteRule {

  def rewrite(ptn: Pattern): Pattern = {
    // First look for union and extract common patterns from it
    modify(ptn, p => p.isInstanceOf[PUnion], update).get
  }

  def update(union: Pattern): Pattern = {
    // flatten the union content
    val unionData = union.asInstanceOf[PUnion].content.map(p => {
      p match {
        case seq: PSeq => seq.content
        case _ => Array(p).toSeq
      }
    }).toSeq
    val cseq = new CommonSeq
    // Look for common sequence
    val seq = cseq.find(unionData, compare)
    if (!seq.isEmpty) {
      // Common Seq split tokens into pieces
      val commonPos = cseq.positions
      val buffers = Array.fill(seq.length + 1)(new ArrayBuffer[Pattern])
      //
      commonPos.zip(unionData).foreach(lp => {
        val pos = lp._1
        val data = lp._2
        val interval = new ArrayBuffer[(Int, Int)]
        var start = 0
        pos.foreach(p => {
          interval += ((start, p._1))
          start = p._2
        })
        interval += ((start, data.length))

        interval.map(p => data.slice(p._1, p._1 + p._2)).zip(buffers)
          .foreach(i => i._2 += new PSeq(i._1))
      })

      // Create new pattern
      val content = new ArrayBuffer[Pattern]
      seq.indices.foreach(i => {
        content += new PUnion(buffers(i))
        content += new PSeq(seq(i))
      })
      content += new PUnion(buffers.last)
      happen
      new PSeq(content)
    } else
      union
  }

  def compare(a: Pattern, b: Pattern): Boolean = {
    (a, b) match {
      case (pta: PToken, ptb: PToken) => {
        if (pta.token.getClass != ptb.token.getClass) {
          false
        } else {
          !pta.token.isInstanceOf[TWord] ||
            pta.token.value.equals(ptb.token.value)
        }
      }
      case (pua: PUnion, pub: PUnion) => {
        // TODO There's no need to compare union, temporarily return false
        false
      }
      case (psa: PSeq, psb: PSeq) => {
        (psa.content.length == psb.content.length) &&
          psa.content.zip(psb.content).map(p => compare(p._1, p._2))
            .reduce((b1, b2) => b1 || b2)
      }
      case _ => {
        false
      }
    }
  }
}
