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
import org.nd4j.linalg.indexing.INDArrayIndex
import scala.collection.mutable.ArrayBuffer

object Index {

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
    val offset = Nd4j.create((0 until rownum)
      .map(_.doubleValue() * rowlength).toArray).reshape(rownum, -1)
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
    val offset = Nd4j.create((0 until rownum)
      .map(_.doubleValue() * rowlength).toArray).reshape(rownum, -1)
    idxflatten.addi(offset)
    val valueflatten = toput.reshape(idxshape.product, -1)

    NDArrayUtil.toInts(idxflatten).zip(valueflatten.data().asDouble())
      .foreach(p => flatten.putScalar(p._1, p._2))
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

  def index(total: Int, axis: Int, idx: Int): Array[INDArrayIndex] = {
    val result = new ArrayBuffer[INDArrayIndex]()
    result ++= (0 until axis).map(i => NDArrayIndex.all())
    result += NDArrayIndex.point(idx)
    result ++= (axis + 1 until total).map(i => NDArrayIndex.all())

    result.toArray
  }
}

object Broadcast {
  /**
   * Broadcast a to the given shape.
   *
   * This method support broadcast from lower dimension to higher dimension.
   * E.g., [a,b] -> [x,y,a,b], but no [a,b]-> [a,b,c,d]
   * 1 can be broadcasted to bigger numbers, e.g., [1,b]->[c,a,b]
   *
   * For same num of dimension, it can do [1,b]->[a,b] and [b,1]->[b,a]
   */
  def broadcast(a: INDArray, shape: Array[Int]): INDArray = {
    var originShape = a.shape()

    if (originShape.sameElements(shape))
      return a

    if (originShape.length == shape.length)
      return a.broadcast(shape: _*)

    val originProd = originShape.product
    val shapeProd = shape.product
    if (originProd > shapeProd || shapeProd % originProd != 0)
      throw new IllegalArgumentException("Cannot broadcast, [%d]->[%d]".format(originProd, shapeProd))

    var newProd = 1

    var lengthDiff = shape.length - originShape.length
    for (i <- shape.length - 1 to 0 by -1) {
      var offsetIdx = i - lengthDiff
      if (offsetIdx >= 0 && originShape(offsetIdx) != 1 && shape(i) != originShape(offsetIdx)) {
        throw new IllegalArgumentException("Different shape: %d@%d<->%d@%d"
          .format(shape(i), i, originShape(offsetIdx), offsetIdx))
      }
      if (offsetIdx < 0 || originShape(offsetIdx) == 1) {
        newProd *= shape(i)
      }
    }
    a.reshape(1, -1).broadcast(newProd, originProd).reshape(shape: _*)
  }

  /**
   * Assume the arrays are broadcast-able. Compute the different axis
   */
  def diff(ashape: Array[Int], bshape: Array[Int]): (Array[Int], Array[Int]) = {
    val maxlen = Math.max(ashape.length, bshape.length)
    val apadded = ashape.reverse.padTo(maxlen, 0).reverse
    val bpadded = bshape.reverse.padTo(maxlen, 0).reverse
    val maxdim = apadded.zipAll(bpadded, 0, 0).map(p => Math.max(p._1, p._2))

    (apadded.zipAll(maxdim, 0, 0).zipWithIndex
      .filter(p => p._1._1 < p._1._2).map(_._2),
      bpadded.zipAll(maxdim, 0, 0).zipWithIndex
      .filter(p => p._1._1 < p._1._2).map(_._2))
  }

}