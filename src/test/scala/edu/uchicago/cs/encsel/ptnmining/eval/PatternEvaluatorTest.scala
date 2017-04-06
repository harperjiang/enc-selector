package edu.uchicago.cs.encsel.ptnmining.eval

import edu.uchicago.cs.encsel.ptnmining.{PSeq, PToken, PUnion}
import edu.uchicago.cs.encsel.ptnmining.parser.{TInt, TSymbol, TWord, Tokenizer}
import org.junit.Test
import org.junit.Assert._

/**
  * Created by harper on 4/6/17.
  */
class PatternEvaluatorTest {

  @Test
  def testEvaluate: Unit = {
    // Pattern Size is 26
    val pattern = new PSeq(
      new PToken(new TWord("good")),
      new PUnion(
        new PToken(new TInt("1")),
        new PToken(new TInt("2")),
        new PToken(new TInt("3"))
      ),
      new PToken(new TSymbol("-")),
      new PUnion(
        new PToken(new TWord("ASM")),
        new PToken(new TWord("MTM")),
        new PToken(new TWord("DDTE")),
        new PSeq(
          new PToken(new TWord("CHO")),
          new PToken(new TSymbol("-")),
          new PUnion(
            new PToken(new TWord("A")),
            new PToken(new TWord("B")),
            new PToken(new TWord("C")),
            new PToken(new TWord("D"))
          )
        )
      )
    )

    val dataset = Seq("good1-ASM", "good2-MTM", "good2-DDTE",
      "good3-CHO-A", "good3-CHO-B", "good3-ASM", "ttmdpt-dawee-323").map(Tokenizer.tokenize(_).toSeq)

    val result = PatternEvaluator.evaluate(pattern, dataset)

    assertEquals(55, result, 0.01)
  }
}
