package edu.uchicago.cs.dist

import java.util.concurrent.ArrayBlockingQueue

/**
  * Created by harper on 5/4/17.
  */
class MemoryChannel extends Channel {

  protected val content = new ArrayBlockingQueue[Serializable](10)

  protected var callback: (Serializable) => Unit = null

  var id: String = ""

  def send(data: Serializable): Unit = {
    if (callback == null)
      content.offer(data)
    else {
      callback(data)
    }
  }

  def listen(): Serializable = content.take()

  def listen(callback: (Serializable) => Unit) = {
    this.callback = callback
  }

}
