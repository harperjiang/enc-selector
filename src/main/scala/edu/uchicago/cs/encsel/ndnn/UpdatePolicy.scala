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

import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.ops.transforms.Transforms

object UpdatePolicy {
  val etaDefault = 0.05

  val momentumKey = "momentum"

  val rmspropKey = "rmsprop"
  val rmsEpsilon = 1e-8

  val adammeanKey = "adammean"
  val adamvarKey = "adamvar"
}

trait UpdatePolicy {
  def update(p: Param): Unit
}

class SGD(e: Double) extends UpdatePolicy {
  val eta = e

  def this() {
    this(UpdatePolicy.etaDefault)
  }

  def update(p: Param) = {
    if (p.grad != null)
      p.value.subi(p.grad.mul(eta))
  }
}

class Momentum(e: Double, m: Double) extends UpdatePolicy {
  val eta = e
  val mu = m

  def update(p: Param) = {
    if (p.grad != null) {
      val grad = p.grad
      val oldmomen = p.context.getOrElse(UpdatePolicy.momentumKey, grad)
      val momentum = oldmomen.mul(mu).add(grad.mul(1 - mu))
      p.value.subi(momentum.mul(eta))
      p.context.put(UpdatePolicy.momentumKey, momentum)
    }
  }
}

class RMSProp(e: Double, b: Double) extends UpdatePolicy {
  val eta = e
  val beta = b

  def update(p: Param) = {
    if (p.grad != null) {
      val grad = p.grad
      val gradsqr = Transforms.pow(grad, 2)
      val oldrms = p.context.getOrElse(UpdatePolicy.rmspropKey, gradsqr)
      val rms = oldrms.mul(beta).add(gradsqr.mul(1 - beta))
      p.value.subi(grad.mul(eta).div(Transforms.sqrt(rms).add(UpdatePolicy.rmsEpsilon)))
      p.context.put(UpdatePolicy.rmspropKey, rms)
    }
  }
}

class Adam(e: Double, a: Double, b: Double) extends UpdatePolicy {
  val eta = e
  val alpha = a
  val beta = b

  def update(p: Param) = {
    if (p.grad != null) {
      val grad = p.grad
      val gradsqr = Transforms.pow(grad, 2)
      val oldmomen = p.context.getOrElse(UpdatePolicy.adammeanKey, grad)
      val momentum = oldmomen.mul(alpha).add(grad.mul(1 - alpha))

      val oldrms = p.context.getOrElse(UpdatePolicy.adamvarKey, gradsqr)
      val rms = oldrms.mul(beta).add(gradsqr.mul(1 - beta))
      p.value.subi(momentum.mul(eta).div(Transforms.sqrt(rms).add(UpdatePolicy.rmsEpsilon)))

      p.context.put(UpdatePolicy.adammeanKey, momentum)
      p.context.put(UpdatePolicy.adamvarKey, rms)
    }
  }
}