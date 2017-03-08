package edu.uchicago.cs.encsel.util

import org.junit.Test

class WordUtilsTest {

  @Test
  def testLevDist2: Unit = {
    val a = "actual"
    val b = "actua"

    println(WordUtils.levDistance2(a, b))
  }
}