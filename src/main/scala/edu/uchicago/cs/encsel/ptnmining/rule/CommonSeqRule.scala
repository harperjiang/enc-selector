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
import edu.uchicago.cs.encsel.ptnmining._

import scala.collection.mutable.ArrayBuffer

/**
  * Look for common sequence from a union and split it into smaller pieces
  *
  */
class CommonSeqRule extends RewriteRule {

  protected def condition(ptn: Pattern): Boolean =
    ptn.isInstanceOf[PUnion] && ptn.asInstanceOf[PUnion].content.size > 1

  protected def update(union: Pattern): Pattern = {
    // flatten the union content
    val unionData = union.asInstanceOf[PUnion].content.map(p => {
      p match {
        case seq: PSeq => seq.content
        case _ => Array(p).toSeq
      }
    })
    val cseq = new CommonSeq
    // Look for common sequence
    val seq = cseq.find(unionData, compare)

    if (seq.nonEmpty) {
      val sectionBuffers = Array.fill(seq.length + 1)(new ArrayBuffer[Pattern])
      val commonPos = cseq.positions
      commonPos.zip(unionData).foreach(lp => {
        val pos = lp._1
        val data = lp._2

        var pointer = 0

        pos.indices.foreach(i => {
          val sec = pos(i)
          sectionBuffers(i) += (sec._1 match {
            case p if p == pointer => PEmpty
            case _ => new PSeq(data.slice(pointer, sec._1))
          })
          pointer = sec._2
        })
        sectionBuffers.last += (pointer match {
          case last if last == data.length => PEmpty
          case _ => new PSeq(data.slice(pointer, data.length))
        })
      })
      // Create new pattern

      val patternSeqs = new ArrayBuffer[Pattern]
      seq.indices.foreach(i => {
        patternSeqs += new PUnion(sectionBuffers(i))
        patternSeqs += new PSeq(seq(i))
      })
      patternSeqs += new PUnion(sectionBuffers.last)

      happen
      new PSeq(patternSeqs)
    } else
      union
  }

  private def compare(a: Pattern, b: Pattern): Boolean = {
    a.equals(b)
  }
}
