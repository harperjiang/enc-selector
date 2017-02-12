package edu.uchicago.cs.encsel.parser

import org.junit.Test
import org.junit.Assert._

class RecordTest {

  @Test
  def testBlankRecord: Unit = {
    var record = new BlankRecord(10)
    assertEquals(10, record.length)
    for (i <- 0 to 9)
      assertEquals("", record(i))
    assertEquals("", record.toString())
    var ite = record.iterator()
    for (i <- 0 to 9) {
      assertTrue(ite.hasNext)
      assertEquals("", ite.next)
    }
    assertTrue(!ite.hasNext)
  }
}