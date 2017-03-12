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

import org.junit.Test
import org.junit.Assert._
import org.nd4j.linalg.factory.Nd4j


import scala.io.Source

/**
  * Created by harper on 3/9/17.
  */
class PatternExtractorTest {

  @Test
  def testExtractFile: Unit = {

    val lines = Source.fromFile("src/test/resource/sample_address").getLines.toSeq

    val extractor = new PatternExtractor

    extractor.extract(lines)
  }

  @Test
  def testSimilar: Unit = {
    val d0 = Nd4j.create(Array(1d, 0))
    val d90 = Nd4j.create(Array(0d, 1))
    val d180 = Nd4j.create(Array(-1d, 0))

    val a = Array(d180, d0, d180)
    val b = Array(d0)

    val similar = PatternExtractor.similar(a, b)

    assertTrue(Array((1,0)).deep == similar.toArray.deep)

    val a2 = Array(d180, d0, d180)
    val b2 = Array(d0, d0)
    val similar2 = PatternExtractor.similar(a2, b2)
    assertTrue(Array((1,0)).deep == similar2.toArray.deep)

    val a3 = Array(d180, d180)
    val b3 = Array(d0, d0)

    val similar3 = PatternExtractor.similar(a3, b3)
    assertTrue(Array.empty[(Int,Int)].deep == similar3.toArray.deep)

    val a4 = Array(d180, d0, d90, d0)
    val b4 = Array(d0, d0)
    val similar4 = PatternExtractor.similar(a4,b4)
    assertTrue(Array((1,0),(3,1)).deep == similar4.toArray.deep)
  }
}
