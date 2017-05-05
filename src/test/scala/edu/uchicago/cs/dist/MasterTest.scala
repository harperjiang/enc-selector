package edu.uchicago.cs.dist

import org.junit.Test

/**
  * Created by harper on 5/4/17.
  */
class MasterTest {

  @Test
  def testSubmit: Unit = {

    val reg = new MemoryChannelRegistry()

    val master = new Master(reg)
  }

  @Test
  def testCollect: Unit = {

  }
}
