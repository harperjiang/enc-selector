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

import edu.uchicago.cs.encsel.ptnmining.parser._

/**
  * Created by harper on 3/16/17.
  */

object Pattern {
  def generate(in: Seq[Seq[Token]]): Pattern = {
    // Generate a direct pattern by translating tokens

    val translated = new PUnion(in.map(l => new PSeq(l.map(new PToken(_)))))

    // Repeatedly refine the pattern using supplied rules
    var toRefine = translated
    while (true) {
      val refined = refine(toRefine)
      if (refined == toRefine)
        return refined
    }
    throw new RuntimeException("Failed to infer a pattern")
  }

  def refine(root: Pattern): Pattern = {

    // First look for union and extract common patterns from it
    val commonseq: Pattern => Unit = (union: Pattern) => {
      if(union.isInstanceOf[PUnion]) {

      }
    }
    traverse(root, commonseq)

    null
  }

  private def traverse(root: Pattern, callback: Pattern => Unit): Unit = {
    root match {
      case token: PToken => {}
      case seq: PSeq => {
        seq.content.foreach(traverse(_, callback))
      }
      case union: PUnion => {
        union.content.foreach(traverse(_, callback))
      }
    }
    callback(root)
  }
}

trait Pattern

class PToken(token: Token) extends Pattern

class PSeq(c: Seq[Pattern]) extends Pattern {
  val content = c
}

class PUnion(c: Seq[Pattern]) extends Pattern {
  val content = c
}

