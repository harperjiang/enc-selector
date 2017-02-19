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
package edu.uchicago.cs.encsel.ndnn

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.indexing.NDArrayIndex
import org.nd4j.linalg.indexing.SpecifiedIndex
import org.nd4j.linalg.util.NDArrayUtil

object Indexer {

  /**
   * Get element in data specified by idx
   *
   * @param data of shape [A,B,C,D,...,M,N]
   * @param idx  of shape [A,B,C,D,...,M,1?]
   */
  def get(data: INDArray, idx: INDArray): INDArray = {

    val datashape = data.shape
    val idxshape = idx.shape
    // Check shape
    if (!checkShape(datashape, idxshape))
      throw new IllegalArgumentException("Incorrect shape")

    val rownum = datashape.dropRight(1).product
    val rowlength = datashape.last

    val flatten = data.reshape(datashape.product, -1)
    val idxflatten = idx.dup.reshape(idxshape.product, -1)
    val offset = Nd4j.create((0 until rownum).map(_.doubleValue() * rowlength).toArray).reshape(rownum, -1)
    idxflatten.addi(offset)

    Nd4j.create(NDArrayUtil.toInts(idxflatten).map { flatten.getDouble(_) }, idxshape)
  }

  def put(data: INDArray, idx: INDArray, toput: INDArray): Unit = {
    val datashape = data.shape
    val idxshape = idx.shape
    val toputshape = toput.shape
    // Check shape
    if (!checkShape(datashape, idxshape) || !checkShape(datashape, toputshape))
      throw new IllegalArgumentException("Incorrect shape")

    val rownum = datashape.dropRight(1).product
    val rowlength = datashape.last

    val flatten = data.reshape(datashape.product, -1)
    val idxflatten = idx.dup.reshape(idxshape.product, -1)
    val offset = Nd4j.create((0 until rownum).map(_.doubleValue() * rowlength).toArray).reshape(rownum, -1)
    idxflatten.addi(offset)
    val valueflatten = toput.reshape(idxshape.product, -1)

    NDArrayUtil.toInts(idxflatten).zip(valueflatten.data().asDouble()).foreach(p => flatten.putScalar(p._1, p._2))
  }

  protected def checkShape(dataShape: Array[Int], idxShape: Array[Int]): Boolean = {

    dataShape.length match {
      case x if x == idxShape.length => {
        for (i <- 0 until dataShape.length - 1) {
          if (dataShape(i) != idxShape(i))
            return false
        }
        if (idxShape.last != 1)
          return false
        return true
      }
      case xp1 if xp1 == idxShape.length + 1 => {
        for (i <- 0 until dataShape.length - 1) {
          if (dataShape(i) != idxShape(i))
            return false
        }
        return true
      }
      case _ => false
    }
  }

  /**
   * TODO
   * Support Numpy style indexing
   */
  def get(data: INDArray, idx: String) = {
    null
  }
}