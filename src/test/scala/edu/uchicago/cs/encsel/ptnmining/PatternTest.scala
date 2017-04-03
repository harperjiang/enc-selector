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

package edu.uchicago.cs.encsel.ptnmining

import edu.uchicago.cs.encsel.ptnmining.parser.{TInt, TWord}
import org.junit.Test
import org.junit.Assert._

/**
  * Created by harper on 3/31/17.
  */
class PatternTest {

  @Test
  def testNaming: Unit = {
    val pattern = new PSeq(new PToken(new TWord("real")),
      new PUnion(new PWordAny, new PIntAny),
      new PToken(new TInt("3324")))

    pattern.naming()

    assertEquals("_0", pattern.getName)
    //noinspection ZeroIndexToHead
    assertEquals("_0_0", pattern.content(0).getName)
    assertEquals("_0_1", pattern.content(1).getName)
    //noinspection ZeroIndexToHead
    assertEquals("_0_1_0", pattern.content(1).asInstanceOf[PUnion].content(0).getName)
    assertEquals("_0_1_1", pattern.content(1).asInstanceOf[PUnion].content(1).getName)
    assertEquals("_0_2", pattern.content(2).getName)
  }
}
