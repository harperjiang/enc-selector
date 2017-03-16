package edu.uchicago.cs.encsel.ptnmining.lexer

import org.junit.Test
import org.junit.Assert._
/**
  * Created by harper on 3/14/17.
  */
class ScannerTest {

  @Test
  def testScan:Unit = {
    val line = "778-9383 Suspendisse Av. Weirton IN 93479 (326) 677-3419"

    val tokens = Scanner.scan(line).toArray

    assertEquals(21, tokens.length)

  }
}
