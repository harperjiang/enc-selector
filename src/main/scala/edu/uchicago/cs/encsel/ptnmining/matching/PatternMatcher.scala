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

package edu.uchicago.cs.encsel.ptnmining.matching

import edu.uchicago.cs.encsel.ptnmining._
import edu.uchicago.cs.encsel.ptnmining.parser.{TDouble, TInt, TWord, Token}


/**
  * Created by harper on 3/31/17.
  */
object PatternMatcher {

  def matchon(ptn: Pattern, tokens: Seq[Token]): Option[Record] = {
    val matchNode = Match.build(ptn)
    var items = matchNode.next

    while (!items.isEmpty) {
      val matched = matchItems(items, tokens)
      if (!matched.isEmpty)
        return matched
      items = matchNode.next
    }
    return None
  }

  def matchItems(ptns: Seq[Pattern], tokens: Seq[Token]): Option[Record] = {
    val record = new Record

    var pointer = 0
    var matched = true
    ptns.foreach(ptn => {
      if (matched) {
        ptn match {
          case PEmpty => {}
          case wany: PWordAny => {
            matched &= tokens(pointer).isInstanceOf[TWord]
            record.add(ptn.getName, tokens(pointer))
            pointer += 1
          }
          case iany: PIntAny => {
            matched &= tokens(pointer).isInstanceOf[TInt]
            record.add(ptn.getName, tokens(pointer))
            pointer += 1
          }
          case dany: PDoubleAny => {
            matched &= tokens(pointer).isInstanceOf[TDouble]
            record.add(ptn.getName, tokens(pointer))
            pointer += 1
          }
          case token: PToken => {
            matched &= tokens(pointer).equals(token.token)
            record.add(ptn.getName, tokens(pointer))
            pointer += 1
          }
          case _ => throw new IllegalArgumentException("Non-matchable Pattern:"
            + ptn.getClass.getSimpleName)
        }
      }
    })
    matched match {
      case true => Some(record)
      case false => None
    }
  }
}

private object Match {
  def build(ptn: Pattern): Match = {
    ptn match {
      case seq: PSeq => new SeqMatch(seq)
      case union: PUnion => new UnionMatch(union)
      case _ => new SimpleMatch(ptn)
    }
  }
}

private trait Match {

  def next: Seq[Pattern]

  def reset: Unit
}

private class SimpleMatch(ptn: Pattern) extends Match {
  private var used = false

  def next = used match {
    case true => Seq.empty[Pattern]
    case false => {
      used = true
      Seq(ptn)
    }
  }

  def reset = used = false
}

private class SeqMatch(seq: PSeq) extends Match {

  val children = seq.content.map(Match.build(_))
  val items = children.map(_.next).toBuffer

  def next = {
    var result = items.flatten.toArray
    if (!result.isEmpty) {
      // Build next
      var pointer = items.length - 1
      var foundNext = false
      while (pointer >= 0 && !foundNext) {
        items.remove(pointer)
        children(pointer).next match {
          case e if e.isEmpty => {
            children(pointer).reset
          }
          case x => {
            items += x
            foundNext = true
            pointer += 2
          }
        }
        pointer -= 1
      }
      if (foundNext) {
        for (i <- pointer until children.length) {
          items += children(i).next
        }
      }
    }
    result
  }

  def reset = children.foreach(_.reset)
}

private class UnionMatch(union: PUnion) extends Match {

  val children = union.content.map(Match.build(_))
  var counter = 0

  def next = {
    children(counter).next match {
      case empty if empty.isEmpty => {
        if (counter < children.length - 1) {
          counter += 1
          next
        } else {
          empty
        }
      }
      case valid => valid
    }
  }

  def reset = {
    children.foreach(_.reset)
    counter = 0
  }
}
