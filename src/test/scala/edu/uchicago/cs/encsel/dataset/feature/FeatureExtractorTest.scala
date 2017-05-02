package edu.uchicago.cs.encsel.dataset.feature

import org.junit.Assert._
import org.junit.Test

/**
  * Created by harper on 4/27/17.
  */
class FeatureExtractorTest {

  @Test
  def testEmptyFilter: Unit = {
    val input = Iterator("a", "b", "c", "d", "e", "f", "g")
    val filtered = FeatureExtractor.emptyFilter(input).toArray
    assertEquals(7, filtered.size)
    for (i <- 0 until 7) {
      assertEquals(('a' + i).toChar.toString, filtered(i))
    }
  }

  @Test
  def testFirstNFilter: Unit = {
    val input = (0 to 1000).map(_.toString).toIterator
    val filtered = FeatureExtractor.firstNFilter(50)(input).toArray
    assertEquals(50, filtered.size)
    for (i <- 0 until 50) {
      assertEquals(filtered(i), i.toString)
    }
  }

  @Test
  def testIidSamplingFilter: Unit = {
    val input = (0 to 5000).map(_.toString).toIterator
    val filtered = FeatureExtractor.iidSamplingFilter(0.1)(input).toArray
    assertTrue(450 <= filtered.size)
    assertTrue(filtered.size <= 550)
    filtered.foreach(i => {
      assertTrue(i.toInt >= 0)
      assertTrue(i.toInt <= 5000)
    })
    val set = filtered.toSet
    assertEquals(set.size, filtered.size)
  }
}


