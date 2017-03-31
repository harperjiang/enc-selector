package edu.uchicago.cs.encsel.ptnmining.rule

import edu.uchicago.cs.encsel.ptnmining.{PToken, Pattern}
import edu.uchicago.cs.encsel.ptnmining.parser.{TWord, Token, Tokenizer}
import org.junit.Assert._
import org.junit.Test

import scala.io.Source

/**
  * Created by harper on 3/14/17.
  */
class CommonSeqTest {

  @Test
  def testBetween: Unit = {
    val a = "778-9383 Suspendisse Av. Weirton IN 93479 (326) 677-3419"
    val b = "Ap #285-7193 Ullamcorper Avenue Amesbury HI 93373 (302) 259-2375"

    val atokens = Tokenizer.tokenize(a).toSeq
    val btokens = Tokenizer.tokenize(b).toSeq

    val cseq = new CommonSeq

    val commons = cseq.between(atokens, btokens, (a: Token, b: Token) => {
      (a.getClass == b.getClass) && (a match {
        case wd: TWord => a.value.equals(b.value)
        case _ => true
      })
    })

    assertEquals(2, commons.size)

    assertEquals((0, 3, 4), commons(0))
    assertEquals((11, 14, 8), commons(1))
  }

  @Test
  def testFind: Unit = {
    val a = Array(Array(1, 2, 3, 4, 5, 6, 7),
      Array(3, 2, 3, 4, 5, 6, 1),
      Array(2, 9, 3, 4, 5, 6, 7),
      Array(2, 3, 5, 6, 7, 2, 1, 0, 5)).map(_.toSeq).toSeq

    val cseq = new CommonSeq
    val common = cseq.find(a, (a: Int, b: Int) => {
      a == b
    })

    assertEquals(2, common.length)
    assertArrayEquals(Array(5, 6), common.toArray)

    val pos = cseq.positions
    assertEquals(4, pos.length)

    assertEquals((4, 2), pos(0))
    assertEquals((4, 2), pos(1))
    assertEquals((4, 2), pos(2))
    assertEquals((2, 2), pos(3))
  }
}

