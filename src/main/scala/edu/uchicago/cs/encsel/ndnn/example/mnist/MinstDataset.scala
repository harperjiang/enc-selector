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
package edu.uchicago.cs.encsel.ndnn.example.mnist

import java.io.DataInputStream
import java.io.FileInputStream

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

import edu.uchicago.cs.encsel.ndnn.DefaultDataset

object MinstDataset {
  val DATA_MAGIC = 2051
  val LABEL_MAGIC = 2049
}

class MinstDataset(trainFile: String, testFile: String) extends DefaultDataset(Array(28 * 28), Array(1)) {

  def load(): (Int, Array[INDArray], Array[INDArray]) = {
    val datais = new DataInputStream(new FileInputStream(trainFile))
    val labelis = new DataInputStream(new FileInputStream(testFile))
    // Magic number
    val datamagic = datais.readInt()
    val labelmagic = labelis.readInt()
    if (MinstDataset.DATA_MAGIC != datamagic || MinstDataset.LABEL_MAGIC != labelmagic)
      throw new IllegalArgumentException("Incorrect magic number: %d, %d".format(datamagic, labelmagic))

    val datacount = datais.readInt()
    val labelcount = labelis.readInt()
    if (datacount != labelcount)
      throw new IllegalArgumentException("Incorrect number of records")

    val rowcnt = datais.readInt()
    val colcnt = datais.readInt()
    if (rowcnt != 28 || colcnt != 28)
      throw new IllegalArgumentException("Incorrect row/col cnt")

    val datas = new Array[INDArray](datacount)
    val labels = new Array[INDArray](datacount)
    val databuffer = new Array[Double](rowcnt * colcnt)
    for (i <- 0 until datacount) {
      for (j <- 0 until databuffer.length)
        databuffer(j) = datais.readUnsignedByte().toDouble
      datas(i) = Nd4j.create(databuffer)
      labels(i) = Nd4j.create(Array(labelis.readUnsignedByte().toDouble))
    }

    datais.close
    labelis.close

    (datacount, datas, labels)
  }
}