package edu.uchicago.cs.dist

import javax.jms.ObjectMessage

import org.junit.Assert._
import org.junit.Test

import scala.collection.mutable.ArrayBuffer

/**
  * Created by harper on 5/5/17.
  */
class JMSChannelRegistryTest {
  @Test
  def testFind: Unit = {
    val reg = new JMSChannelRegistry("tcp://localhost:61616")

    val channel = reg.find("a")

  }
}

class JMSChannelTest {

  val reg = new JMSChannelRegistry("tcp://localhost:61616")

  @Test
  def testSend: Unit = {
    val channel = reg.find("testSend").asInstanceOf[JMSChannel]

    channel.send("test string")
    val message = channel.consumer.receive()

    assertTrue(message.isInstanceOf[ObjectMessage])

    assertEquals("test string", message.asInstanceOf[ObjectMessage].getObject)
  }

  @Test
  def testListen1: Unit = {
    val channel = reg.find("testListen1").asInstanceOf[JMSChannel]

    channel.send("test string")

    assertEquals("test string", channel.listen())

    channel.send("test string1")
    channel.send("test string2")
    assertEquals("test string1", channel.listen())
    assertEquals("test string2", channel.listen())
  }

  @Test
  def testListen2: Unit = {

    val channel = reg.find("testListen2").asInstanceOf[JMSChannel]

    var buffer = new ArrayBuffer[String]

    channel.listen((a: java.io.Serializable) => {
      buffer += a.toString
    })

    channel.send("test string 1")
    channel.send("test string 2")
    channel.send("test string 3")

    assertEquals(3,buffer.size)
    assertEquals("test string 1", buffer(0))
    assertEquals("test string 2", buffer(1))
    assertEquals("test string 3", buffer(2))
  }
}
