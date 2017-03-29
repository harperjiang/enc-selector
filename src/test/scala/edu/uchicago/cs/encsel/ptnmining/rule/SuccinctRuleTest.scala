package edu.uchicago.cs.encsel.ptnmining.rule

import edu.uchicago.cs.encsel.ptnmining.parser.{TInt, TWord}
import edu.uchicago.cs.encsel.ptnmining.{PEmpty, PSeq, PToken, PUnion}
import org.junit.Assert._
/**
  * Created by harper on 3/29/17.
  */
class SuccinctRuleTest {

  def testRewrite: Unit = {
    val pattern = new PSeq(Array(
      new PUnion(Array(PEmpty, new PToken(new TWord("ddd")))),
      new PSeq(Array(new PToken(new TInt("32342")))),
      new PUnion(Array(new PToken(new TWord("abc")), new PToken(new TWord("abc"))))))

    val rule = new SuccinctRule

    rule.rewrite(pattern)

    assertTrue(rule.happened)
  }
}
