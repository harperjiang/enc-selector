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

/**
  * Created by harper on 3/13/17.
  */
class PhraseTest {

  @Test
  def testChildren: Unit = {

    val children = Phrase.children(Array("a", "b", "c", "d", "e", "f", "g"))
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
}
