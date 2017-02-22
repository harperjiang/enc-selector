package edu.uchicago.cs.encsel.ndnn.example.lstm

import edu.uchicago.cs.encsel.ndnn.Graph
import edu.uchicago.cs.encsel.ndnn.SGD
import edu.uchicago.cs.encsel.ndnn.Xavier
import edu.uchicago.cs.encsel.ndnn.SoftMaxLogLoss
import edu.uchicago.cs.encsel.ndnn.Param
import edu.uchicago.cs.encsel.ndnn.Zero
import scala.collection.mutable.ArrayBuffer
import edu.uchicago.cs.encsel.ndnn.rnn.LSTMCell
import edu.uchicago.cs.encsel.ndnn.Input
import edu.uchicago.cs.encsel.ndnn.DotMul
import edu.uchicago.cs.encsel.ndnn.Embed

class LayerLSTMGraph(layer: Int, numChar: Int, hiddenDim: Int) extends Graph(Xavier, new SGD(0.05, 1), new SoftMaxLogLoss) {

  val c2v = param("c2v", Array(numChar, hiddenDim))
  val v2c = param("v2c", Array(hiddenDim, numChar))
  val wf = new Array[Param](layer)
  val bf = new Array[Param](layer)
  val wi = new Array[Param](layer)
  val bi = new Array[Param](layer)
  val wc = new Array[Param](layer)
  val bc = new Array[Param](layer)
  val wo = new Array[Param](layer)
  val bo = new Array[Param](layer)

  val h0 = new Array[Param](layer)
  val c0 = new Array[Param](layer)

  val cells = new ArrayBuffer[Array[LSTMCell]]()

  {
    val inputSize = 2 * hiddenDim
    for (i <- 0 until layer) {
      wf(i) = param("wf_%d".format(i), Array(inputSize, hiddenDim))
      bf(i) = param("bf_%d".format(i), Array(inputSize, hiddenDim))(Zero)
      wi(i) = param("wi_%d".format(i), Array(inputSize, hiddenDim))
      bi(i) = param("bi_%d".format(i), Array(inputSize, hiddenDim))(Zero)
      wc(i) = param("wc_%d".format(i), Array(inputSize, hiddenDim))
      bc(i) = param("bc_%d".format(i), Array(inputSize, hiddenDim))(Zero)
      wo(i) = param("wo_%d".format(i), Array(inputSize, hiddenDim))
      bo(i) = param("bo_%d".format(i), Array(inputSize, hiddenDim))(Zero)
      h0(i) = param("h0_%d".format(i), Array(1, hiddenDim))(Zero)
      c0(i) = param("c0_%d".format(i), Array(1, hiddenDim))(Zero)
    }
  }

  val textInput = new ArrayBuffer[Input]()

  // Extend RNN to the expected size and build connections between cells
  def extend(length: Int): Unit = {
    if (length > cells.length) {
      // Expand the network
      for (i <- cells.length until length) {
        val newNodes = new Array[LSTMCell](layer)
        for (j <- 0 until layer) {
          val h = i match { case 0 => h0(j) case x => cells(i - 1)(j).hout }
          val c = i match { case 0 => c0(j) case x => cells(i - 1)(j).cout }
          val in = j match {
            case 0 => {
              val in = new Input("%d".format(i))
              val mapped = new Embed(in, c2v)
              textInput += in
              mapped
            } case x => cells(i)(j - 1).hout
          }
          newNodes(j) = new LSTMCell(wf(j), bf(j), wi(j), bi(j), wc(j), bc(j), wo(j), bo(j), in, h, c)
        }
        output(newNodes(layer - 1).hout)
        cells += newNodes
      }
    }
    // Remove excessive output
    (length until outputs.length).foreach(i => outputs.remove(length))
  }
}