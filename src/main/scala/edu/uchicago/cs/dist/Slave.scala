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
 *
 */

package edu.uchicago.cs.dist

import java.util.concurrent.Executors

/**
  * Created by harper on 5/3/17.
  */

object Slave extends App {
  new Slave(ChannelRegistry.get).start()
  while (true) {
    Thread.sleep(1000)
  }
}


class Slave(val registry: ChannelRegistry) {

  val receiveChannel = registry.find("distribute")
  val feedbackChannel = registry.find("collect")

  val threadPool = Executors.newFixedThreadPool(10)

  def start(): Unit = {
    receiveChannel.listen((task: Serializable) => {
      if (task.isInstanceOf[TaskPiece]) {
        val piece = task.asInstanceOf[TaskPiece]
        threadPool.submit(new TaskRunnable(this, piece))
      }
    })
  }

  def taskDone(piece: TaskPiece): Unit = {
    feedbackChannel.send(piece)
  }
}

class TaskRunnable(val slave: Slave, val piece: TaskPiece) extends Runnable {

  override def run(): Unit = {
    piece.execute()
    slave.taskDone(piece)

  }
}
