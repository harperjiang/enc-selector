package edu.uchicago.cs.encsel.wordvec

import org.junit.Test
import org.junit.Assert._

class PluralTest {

  @Test
  def testPlural: Unit = {

    assertEquals("code",Plural.removePlural("codes"))
    assertEquals("tree",Plural.removePlural("trees"))
    assertEquals("cake",Plural.removePlural("cakes"))
    assertEquals("pie",Plural.removePlural("pies"))
    
  }
}