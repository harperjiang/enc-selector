package edu.uchicago.cs.encsel.ptnmining.rule

import edu.uchicago.cs.encsel.ptnmining.parser.{TInt, TSymbol, TWord}
import edu.uchicago.cs.encsel.ptnmining.{PSeq, PToken, PUnion}
import org.junit.Test
import org.junit.Assert._

/**
  * Created by harper on 3/29/17.
  */
class CommonSeqRuleTest {

  @Test
  def testRewrite: Unit = {
    val union = new PUnion(Array(
      new PSeq(Array(new PToken(new TWord("abc")), new PToken(new TInt("312")),
        new PToken(new TSymbol("-")), new PToken(new TInt("212")),
        new PToken(new TWord("good")))),
      new PSeq(Array(new PToken(new TInt("4021")), new PToken(new TSymbol("-")),
        new PToken(new TInt("2213")), new PToken(new TWord("akka")),
        new PToken(new TInt("420")))),
      new PSeq(Array(new PToken(new TWord("kwmt")), new PToken(new TWord("ddmpt")),
        new PToken(new TInt("2323")), new PToken(new TSymbol("-")),
        new PToken(new TInt("33130")))),
      new PSeq(Array(new PToken(new TWord("ttpt")), new PToken(new TInt("3232")),
        new PToken(new TSymbol("-")), new PToken(new TInt("42429")),
        new PToken(new TWord("dddd"))))))
    val csq = new CommonSeqRule

    val newptn = csq.rewrite(union)

    assertTrue(csq.happened)

    assertTrue(newptn.isInstanceOf[PSeq])
    val newseq = newptn.asInstanceOf[PSeq]

    assertEquals(3, newseq.content.length)

    assertTrue(newseq.content(0).isInstanceOf[PUnion])
    assertTrue(newseq.content(1).isInstanceOf[PSeq])
    assertTrue(newseq.content(2).isInstanceOf[PUnion])
  }

}
