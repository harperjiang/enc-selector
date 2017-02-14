package edu.uchicago.cs.encsel.wordvec

import org.junit.Test

class WordSplitTest {

  @Test
  def testSplit(): Unit = {
    var split = new WordSplit()
    var res = split.split("RPTDT")
    println(res)
  }
}