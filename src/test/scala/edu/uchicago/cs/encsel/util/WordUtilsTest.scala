package edu.uchicago.cs.encsel.util

import org.junit.Test

class WordUtilsTest {

  @Test
  def testLevDist2: Unit = {
    var a = "actual"
    var b = "actua"

    println(WordUtils.levDistance2(a, b))
  }
}