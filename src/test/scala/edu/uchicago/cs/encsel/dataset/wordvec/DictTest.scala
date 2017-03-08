package edu.uchicago.cs.encsel.dataset.wordvec

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.util.WordUtils

class DictTest {

  @Test
  def testLookup(): Unit = {
    val book = Dict.lookup("book")
    assertEquals("book", book._1)
    val rpt = Dict.lookup("rpt")
    assertEquals("report", rpt._1)
    val cmd = Dict.lookup("cmd")
    println(cmd)
    assertEquals("command", cmd._1)
    val yr = Dict.lookup("yr")
    assertEquals("year", yr._1)
    val dt = Dict.lookup("dt")
    assertEquals("date", dt._1)
    val zip = Dict.lookup("zip")
    assertEquals("zip", zip._1)
    val non = Dict.lookup("jiang")
    println(non)
    assertEquals("jiang", non._1)
  }

  @Test
  def testAbbrv: Unit = {
    assertEquals("rpt", Dict.abbreviate("repeat"))
    assertEquals("rpt", Dict.abbreviate("report"))
  }

  @Test
  def testCorrectWord: Unit = {
    assertEquals("identification", Dict.lookup("identificaiton")._1)
  }

  @Test
  def testPlural: Unit = {
    val codes = Dict.lookup("codes")
    assertEquals("code", codes._1)
  }
}