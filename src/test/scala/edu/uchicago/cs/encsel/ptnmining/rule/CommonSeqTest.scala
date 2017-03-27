package edu.uchicago.cs.encsel.ptnmining.rule

import edu.uchicago.cs.encsel.ptnmining.parser.Scanner
import org.junit.Assert._
import org.junit.Test

import scala.io.Source

/**
  * Created by harper on 3/14/17.
  */
class CommonSeqTest {

  @Test
  def testFind: Unit = {
    val a = "778-9383 Suspendisse Av. Weirton IN 93479 (326) 677-3419"
    val b = "Ap #285-7193 Ullamcorper Avenue Amesbury HI 93373 (302) 259-2375"

    val atokens = Scanner.scan(a).toSeq
    val btokens = Scanner.scan(b).toSeq

    val common = CommonSeq.find(atokens, btokens)

    assertEquals(2, common.size)
  }

  @Test
  def testDiscover:Unit = {

    val lines = Source.fromFile("src/test/resource/sample_address").getLines.toSeq

    val extractor = new CommonSeq

    val result = extractor.discover(lines)

    val pattern = result.asInstanceOf[RegexPattern]
  }
}

