package edu.uchicago.cs.encsel.colpattern

import edu.uchicago.cs.encsel.colpattern.lexer.Sym
import org.junit.Test
import org.junit.Assert._

import scala.io.Source

/**
  * Created by harper on 3/14/17.
  */
class CommonSeqTest {

  @Test
  def testFind: Unit = {
    val a = "778-9383 Suspendisse Av. Weirton IN 93479 (326) 677-3419"
    val b = "Ap #285-7193 Ullamcorper Avenue Amesbury HI 93373 (302) 259-2375"

    val atokens = lexer.Scanner.scan(a).toSeq
    val btokens = lexer.Scanner.scan(b).toSeq

    val common = CommonSeq.find(atokens, btokens)

    assertEquals(2, common.size)
  }

  @Test
  def testExtractFile: Unit = {

    val lines = Source.fromFile("src/test/resource/sample_address").getLines.toSeq

    val extractor = new CommonSeq

    val result = extractor.extract(lines)
    assertEquals(1, result.size)
    val symarray = result(0).toArray
    assertEquals(10, symarray.length)
    assertEquals(Sym.SPACE, symarray(0).sym)
    assertEquals(Sym.INTEGER, symarray(1).sym)
    assertEquals(Sym.SPACE, symarray(2).sym)
    assertEquals(Sym.LPARA, symarray(3).sym)
    assertEquals(Sym.INTEGER, symarray(4).sym)
    assertEquals(Sym.RPARA, symarray(5).sym)
    assertEquals(Sym.SPACE, symarray(6).sym)
    assertEquals(Sym.INTEGER, symarray(7).sym)
    assertEquals(Sym.DASH, symarray(8).sym)
    assertEquals(Sym.INTEGER, symarray(9).sym)
  }
}

