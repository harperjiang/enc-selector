package edu.uchicago.cs.encsel.wordvec

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.util.WordUtils

class DictTest {

  @Test
  def testLookup(): Unit = {
    var book = Dict.lookup("book")
    assertEquals("book", book._1)
    var rpt = Dict.lookup("rpt")
    assertEquals("report", rpt._1)
    var cmd = Dict.lookup("cmd")
    assertEquals("command", cmd._1)
    var yr = Dict.lookup("yr")
    assertEquals("year", yr._1)
    var dt = Dict.lookup("dt")
    assertEquals("date", dt._1)
    var zip = Dict.lookup("zip")
    assertEquals("zip", zip._1)
    var non = Dict.lookup("jiang")
    assertEquals("jiang", non._1)
  }

  @Test
  def testAbbrv: Unit = {
    assertEquals("rpt", Dict.abbrv("repeat"))
    assertEquals("rpt", Dict.abbrv("report"))
  }

  @Test
  def testPlural: Unit = {
    var codes = Dict.lookup("codes")

    assertEquals("code", codes._1)
  }
}