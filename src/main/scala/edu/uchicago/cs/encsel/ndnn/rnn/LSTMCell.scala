package edu.uchicago.cs.encsel.ndnn.rnn

import org.nd4j.linalg.api.ndarray.INDArray
import edu.uchicago.cs.encsel.ndnn.Node
import edu.uchicago.cs.encsel.ndnn.Param
import edu.uchicago.cs.encsel.ndnn.Input
import edu.uchicago.cs.encsel.ndnn.DotMul
import edu.uchicago.cs.encsel.ndnn.Sigmoid
import edu.uchicago.cs.encsel.ndnn.Add
import edu.uchicago.cs.encsel.ndnn.Tanh
import edu.uchicago.cs.encsel.ndnn.Concat
import edu.uchicago.cs.encsel.ndnn.Concat
import edu.uchicago.cs.encsel.ndnn.Concat
import edu.uchicago.cs.encsel.ndnn.Tanh
import edu.uchicago.cs.encsel.ndnn.Mul

class LSTMCell(wf: Param, bf: Param, wi: Param, bi: Param, wc: Param, bc: Param, wo: Param, bo: Param, h: Node, c: Node) {
  val x = new Input("x")
  private var co: Node = _
  private var ho: Node = _

  {
    val concat = new Concat(h, x)
    val fgate = new Sigmoid(new Add(new DotMul(concat, wf), bf))
    val igate = new Sigmoid(new Add(new DotMul(concat, wi), bi))
    val cgate = new Mul(new Tanh(new Add(new DotMul(concat, wc), bc)), igate)
    val ogate = new Sigmoid(new Add(new DotMul(concat, wo), bo))

    co = new Add(new Mul(c, fgate), cgate)
    ho = new Mul(new Tanh(co), ogate)
  }

  def cout = co
  def hout = ho
}