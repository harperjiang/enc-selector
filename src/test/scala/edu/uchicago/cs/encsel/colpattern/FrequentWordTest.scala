/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License,
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 */

package edu.uchicago.cs.encsel.colpattern

import edu.uchicago.cs.encsel.util.WordUtils
import org.junit.Test
import org.junit.Assert._
import org.nd4j.linalg.factory.Nd4j

import scala.io.Source

/**
  * Created by harper on 3/9/17.
  */
class FrequentWordTest {

  @Test
  def testExtractFile: Unit = {

    val lines = Source.fromFile("src/test/resource/sample_address").getLines.toSeq

    val extractor = new FrequentWord

    extractor.extract(lines)
  }

  @Test
  def testMerge: Unit = {

    val hspots = Array(Array(("p.o.", 0, 0.1), ("box", 1, 0.1), ("street", 4, 0.4), ("kk", 5, 0.3)),
      Array(("ap", 1, 0.1), ("e", 3, 0.1), ("st.", 5, 0.1)), Array(("n", 1, 0.1), ("avenue", 3, 0.1), ("pm", 4, 0.2))).map(_.toSeq)
    val finder = new FrequentWord
    val merged = finder.merge(hspots)

    assertEquals(3, merged.length)

    assertTrue(Array("p.o. box", "street kk").deep == merged(0).toArray.deep)
    assertTrue(Array("ap", "e", "st.").deep == merged(1).toArray.deep)
    assertTrue(Array("n", "avenue pm").deep == merged(2).toArray.deep)
  }

  @Test
  def testGroup: Unit = {

  }

  @Test
  def testChildren: Unit = {

    val children = FrequentWord.children(Array("a", "b", "c", "d", "e", "f", "g"))
      .map(_._1.map(_.asInstanceOf[AnyRef]).toArray).toArray

    assertArrayEquals(Array[AnyRef]("a", "b", "c", "d", "e", "f", "g"), children(0))
    assertArrayEquals(Array[AnyRef]("a", "b", "c", "d", "e", "f"), children(1))
    assertArrayEquals(Array[AnyRef]("b", "c", "d", "e", "f", "g"), children(2))
    assertArrayEquals(Array[AnyRef]("a", "b", "c", "d", "e"), children(3))
    assertArrayEquals(Array[AnyRef]("b", "c", "d", "e", "f"), children(4))
    assertArrayEquals(Array[AnyRef]("c", "d", "e", "f", "g"), children(5))
    assertArrayEquals(Array[AnyRef]("a", "b", "c", "d"), children(6))
    assertArrayEquals(Array[AnyRef]("b", "c", "d", "e"), children(7))
    assertArrayEquals(Array[AnyRef]("c", "d", "e", "f"), children(8))
    assertArrayEquals(Array[AnyRef]("d", "e", "f", "g"), children(9))
    assertArrayEquals(Array[AnyRef]("a", "b", "c"), children(10))
  }

  @Test
  def testSimilar: Unit = {
    val d0 = Nd4j.create(Array(1d, 0))
    val d90 = Nd4j.create(Array(0d, 1))
    val d180 = Nd4j.create(Array(-1d, 0))

    val a = Array(d180, d0, d180)
    val b = Array(d0)

    val similar = FrequentWord.similar(a, b)

    assertTrue(Array((1,0)).deep == similar.toArray.deep)

    val a2 = Array(d180, d0, d180)
    val b2 = Array(d0, d0)
    val similar2 = FrequentWord.similar(a2, b2)
    assertTrue(Array((1,0)).deep == similar2.toArray.deep)

    val a3 = Array(d180, d180)
    val b3 = Array(d0, d0)

    val similar3 = FrequentWord.similar(a3, b3)
    assertTrue(Array.empty[(Int,Int)].deep == similar3.toArray.deep)

    val a4 = Array(d180, d0, d90, d0)
    val b4 = Array(d0, d0)
    val similar4 = FrequentWord.similar(a4,b4)
    assertTrue(Array((1,0),(3,1)).deep == similar4.toArray.deep)
  }

}
