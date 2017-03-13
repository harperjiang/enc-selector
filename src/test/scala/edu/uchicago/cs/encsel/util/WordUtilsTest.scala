package edu.uchicago.cs.encsel.util

import org.junit.Test
import org.junit.Assert._
import org.nd4j.linalg.factory.Nd4j

class WordUtilsTest {

  @Test
  def testLevDist2: Unit = {
    val a = "actual"
    val b = "actua"

    println(WordUtils.levDistance2(a, b))
  }
  
  @Test
  def testSimilar: Unit = {
    val d0 = Nd4j.create(Array(1d, 0))
    val d90 = Nd4j.create(Array(0d, 1))
    val d180 = Nd4j.create(Array(-1d, 0))

    val a = Array(d180, d0, d180)
    val b = Array(d0)

    val similar = WordUtils.similar(a, b)

    assertTrue(Array((1,0)).deep == similar.toArray.deep)

    val a2 = Array(d180, d0, d180)
    val b2 = Array(d0, d0)
    val similar2 = WordUtils.similar(a2, b2)
    assertTrue(Array((1,0)).deep == similar2.toArray.deep)

    val a3 = Array(d180, d180)
    val b3 = Array(d0, d0)

    val similar3 = WordUtils.similar(a3, b3)
    assertTrue(Array.empty[(Int,Int)].deep == similar3.toArray.deep)

    val a4 = Array(d180, d0, d90, d0)
    val b4 = Array(d0, d0)
    val similar4 = WordUtils.similar(a4,b4)
    assertTrue(Array((1,0),(3,1)).deep == similar4.toArray.deep)
  }
}