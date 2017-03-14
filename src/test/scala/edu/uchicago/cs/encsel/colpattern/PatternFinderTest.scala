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
class PatternFinderTest {

  @Test
  def testExtractFile: Unit = {

    val lines = Source.fromFile("src/test/resource/sample_address").getLines.toSeq

    val extractor = new PatternFinder

    extractor.extract(lines)
  }

  @Test
  def testMerge: Unit = {

    val hspots = Array(Array(("p.o.", 0, 0.1), ("box", 1, 0.1), ("street", 4, 0.4), ("kk", 5, 0.3)),
      Array(("ap", 1, 0.1), ("e", 3, 0.1), ("st.", 5, 0.1)), Array(("n", 1, 0.1), ("avenue", 3, 0.1), ("pm", 4, 0.2))).map(_.toSeq)
    val finder = new PatternFinder
    val merged = finder.merge(hspots)

    assertEquals(3, merged.length)

    assertTrue(Array("p.o. box", "street kk").deep == merged(0).toArray.deep)
    assertTrue(Array("ap", "e", "st.").deep == merged(1).toArray.deep)
    assertTrue(Array("n", "avenue pm").deep == merged(2).toArray.deep)
  }

  @Test
  def testGroup: Unit = {

  }

}
