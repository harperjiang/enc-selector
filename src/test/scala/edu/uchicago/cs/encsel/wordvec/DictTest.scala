package edu.uchicago.cs.encsel.wordvec

import org.junit.Test
import org.junit.Assert._

class DictTest {

  @Test
  def testLookup(): Unit = {
    var book = Dict.lookup("book")
    println(book)
    var rpt = Dict.lookup("rpt")
    println(rpt)
    var cmd = Dict.lookup("cmd")
    println(cmd)
    var yr = Dict.lookup("yr")
    println(yr)
    var dt = Dict.lookup("dt")
    println(dt)
  }

  @Test
  def testAbbrv: Unit = {
    assertEquals("rpt", Dict.abbrv("repeat"))
    assertEquals("rpt", Dict.abbrv("report"))
  }
}