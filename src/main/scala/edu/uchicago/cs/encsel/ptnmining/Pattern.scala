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
import edu.uchicago.cs.encsel.ptnmining.rule.{CommonSeqRule, SuccinctRule}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by harper on 3/16/17.
  */

object Pattern {

  val rules = Array(new CommonSeqRule, new SuccinctRule)

  def generate(in: Seq[Seq[Token]]): Pattern = {
    // Generate a direct pattern by translating tokens

    val translated = new PUnion(in.map(l => new PSeq(l.map(new PToken(_)): _*)))

    // Repeatedly refine the pattern using supplied rules
    var toRefine: Pattern = translated
    var needRefine = true
    var refineResult: Pattern = toRefine
    while (needRefine) {
      val refined = refine(toRefine)
      if (refined._2) {
        toRefine = refined._1
      } else {
        needRefine = false
        refineResult = refined._1
      }
    }

    val generalized = generalize(refineResult)
    val named = naming(generalized)
    named
  }

  protected def refine(root: Pattern): (Pattern, Boolean) = {
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

  def generalize(ptn: Pattern): Pattern = {
    ptn
  }

  def naming(ptn: Pattern): Pattern = {
    val namingV = new PatternVisitor {

      var counter = new mutable.Stack[Int]
      counter.push(0)

      override def on(ptn: Pattern): Unit = {
        val parentName = path.isEmpty match {
          case true => ""
          case false => path(0).name
        }
        var current = counter.pop
        ptn.name = "%s_%d".format(parentName, current)
        current += 1
        counter.push(current)
      }

      override def enter(container: Pattern): Unit = {
        super.enter(container)
        counter.push(0)
      }

      override def exit(container: Pattern): Unit = {
        super.exit(container)
        counter.pop
      }
    }
    ptn.visit(namingV)
    ptn
  }
}

trait PatternVisitor {

  protected val path = new mutable.Stack[Pattern]

  def on(ptn: Pattern): Unit

  def enter(container: Pattern): Unit = path.push(container)

  def exit(container: Pattern): Unit = path.pop()
}


trait Pattern {
  private[ptnmining] var name = ""

  def getName = name

  /**
    * @return all leaf patterns
    */
  def flatten: Seq[Pattern] = Seq(this)

  /**
    * Recursively visit the pattern elements starting from the root
    *
    * @param visitor
    */
  def visit(visitor: PatternVisitor): Unit = visitor.on(this)
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

class PSeq(cnt: Pattern*) extends Pattern {
  val content = cnt

  def this(ps: Traversable[Pattern]) = this(ps.toSeq: _*)

  override def equals(obj: scala.Any): Boolean = {
    if (obj.isInstanceOf[PSeq]) {
      val seq = obj.asInstanceOf[PSeq]
      return seq.content.equals(content)
    }
    return super.equals(obj)
  }

  override def flatten: Seq[Pattern] = content.flatMap(_.flatten)

  override def visit(visitor: PatternVisitor): Unit = {
    visitor.on(this)
    visitor.enter(this)
    content.foreach(_.visit(visitor))
    visitor.exit(this)
  }
}

class PUnion(cnt: Pattern*) extends Pattern {
  val content = cnt.toSet.toSeq

  def this(c: Traversable[Pattern]) = this(c.toSeq: _*)

  override def equals(obj: scala.Any): Boolean = {
    if (obj.isInstanceOf[PUnion]) {
      val union = obj.asInstanceOf[PUnion]
      return union.content.equals(content)
    }
    return super.equals(obj)
  }

  override def flatten: Seq[Pattern] = content.flatMap(_.flatten)

  override def visit(visitor: PatternVisitor): Unit = {
    visitor.on(this)
    visitor.enter(this)
    content.foreach(_.visit(visitor))
    visitor.exit(this)
  }
}

object PEmpty extends Pattern

class PAny extends Pattern

class PWordAny extends PAny

class PIntAny extends PAny

class PDoubleAny extends PAny