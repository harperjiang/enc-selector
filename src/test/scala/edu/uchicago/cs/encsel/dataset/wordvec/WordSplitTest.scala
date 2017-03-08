package edu.uchicago.cs.encsel.dataset.wordvec

import org.junit.Test
import org.junit.Assert._


class WordSplitTest {

  @Test
  def testSplitAbbrv(): Unit = {
    val split = new WordSplit()
    var res = split.split("RPTYR")
    assertEquals(2, res._1.length)
    assertEquals("report", res._1(0))
    assertEquals("year", res._1(1))

    res = split.split("SMYS")
    assertEquals(2, res._1.length)
    assertEquals("some", res._1(0))
    assertEquals("year", res._1(1))
  }

  @Test
  def testSplitLong: Unit = {
    val split = new WordSplit()
    var res = split.split("inspectioncode")
    assertEquals(2, res._1.length)
    assertEquals("inspection", res._1(0))
    assertEquals("code", res._1(1))

    res = split.split("inspectioncodes")
    assertEquals(2, res._1.length)
    assertEquals("inspection", res._1(0))
    assertEquals("code", res._1(1))
  }
  
  @Test
  def testSplitCombined: Unit = {
    val split = new WordSplit()
    val res = split.split("actualcmd")
    println(res)
    assertEquals(2, res._1.length)
    assertEquals("actual", res._1(0))
    assertEquals("command", res._1(1))
  }

  @Test
  def testSplitPlural: Unit = {
    val split = new WordSplit()
    val res = split.split("ReportYears")
    assertEquals(2, res._1.length)
    assertEquals("report", res._1(0))
    assertEquals("year", res._1(1))
  }

  @Test
  def testRemoveNumber: Unit = {
    val split = new WordSplit()
    val res = split.split("column522342Day")
    assertEquals(2, res._1.length)
    assertEquals("column", res._1(0))
    assertEquals("day", res._1(1))
  }

  @Test
  def testSplitCamel: Unit = {
    val split = new WordSplit()
    var res = split.split("NOVIssuedDate")
    assertEquals(3, res._1.length)
    assertEquals("nov", res._1(0))
    assertEquals("issued", res._1(1))
    assertEquals("date", res._1(2))

    res = split.split("GeneralID")
    assertEquals(2, res._1.length)
    assertEquals("general", res._1(0))
    assertEquals("id", res._1(1))
  }
}