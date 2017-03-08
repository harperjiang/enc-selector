/**
 * *****************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 *     Hao Jiang - initial API and implementation
 *
 * *****************************************************************************
 */

package edu.uchicago.cs.encsel.app

import edu.uchicago.cs.encsel.dataset.persist.Persistence
import edu.uchicago.cs.encsel.dataset.persist.jpa.JPAPersistence
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMGraph

object ContentPredict extends App {

  val jpa = Persistence.get.asInstanceOf[JPAPersistence]
  val column = jpa.find(args(0).toInt)

  val batchSize = 50
  val dataset = new LSTMDataset(column.colFile)
  dataset.batches(batchSize).foreach(batch => {

  })
  
  /*
  graph.c2v.value = Nd4j.readNumpy("/home/harper/dataset/numpy/c2v.npy")
  graph.wf.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wf.npy")
  graph.bf.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bf.npy")
  graph.wi.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wi.npy")
  graph.bi.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bi.npy")
  graph.wc.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wc.npy")
  graph.bc.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bc.npy")
  graph.wo.value = Nd4j.readNumpy("/home/harper/dataset/numpy/wo.npy")
  graph.bo.value = Nd4j.readNumpy("/home/harper/dataset/numpy/bo.npy")
  graph.v2c.value = Nd4j.readNumpy("/home/harper/dataset/numpy/v.npy")
*/
  }
