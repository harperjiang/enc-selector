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
import edu.uchicago.cs.encsel.ptnmining.rule.CommonSeqRule

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by harper on 3/16/17.
  */

object Pattern {

  val rules = Array(new CommonSeqRule)

  def generate(in: Seq[Seq[Token]]): Pattern = {
    // Generate a direct pattern by translating tokens

    val translated = new PUnion(in.map(l => new PSeq(l.map(new PToken(_)))))

    // Repeatedly refine the pattern using supplied rules
    var toRefine: Pattern = translated
    while (true) {
      val refined = refine(toRefine)
      if (refined._2) {
        toRefine = refined._1
      } else {
        return refined._1
      }
    }
    throw new RuntimeException("Failed to infer a pattern")
  }

  def refine(root: Pattern): (Pattern, Boolean) = {
    var current = root

    rules.indices.foreach(i => {
      current = rules(i).rewrite(current)
      if (rules(i).happened) {
        // Apply the first valid rule
        return (current, true)
      }
    })
    return (root, false)
  }
}

trait Pattern {
}

class PToken(t: Token) extends Pattern {
  val token = t

  override def equals(obj: scala.Any): Boolean = {
    if (obj.isInstanceOf[PToken]) {
      val t = obj.asInstanceOf[PToken]
      return t.token.equals(token)
    }
    return super.equals(obj)
  }
}

class PSeq(c: Seq[Pattern]) extends Pattern {
  val content = c

  override def equals(obj: scala.Any): Boolean = {
    if (obj.isInstanceOf[PSeq]) {
      val seq = obj.asInstanceOf[PSeq]
      return seq.content.equals(content)
    }
    return super.equals(obj)
  }
}

class PUnion(c: Seq[Pattern]) extends Pattern {
  val content = c.toSet

  override def equals(obj: scala.Any): Boolean = {
    if (obj.isInstanceOf[PUnion]) {
      val union = obj.asInstanceOf[PUnion]
      return union.content.equals(content)
    }
    return super.equals(obj)
  }

}

