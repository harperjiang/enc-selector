package edu.uchicago.cs.encsel.ndnn.rnn

import org.junit.Test
import org.junit.Assert._
import edu.uchicago.cs.encsel.ndnn.Param
import edu.uchicago.cs.encsel.ndnn.Input
import org.nd4j.linalg.factory.Nd4j
import edu.uchicago.cs.encsel.ndnn.NodeEnv

class LSTMCellTest {

  @Test
  def testBuildCell: Unit = {
    val env = new NodeEnv() {}
    val hd = 3
    val wf = new Param("wf")
    wf.setEnv(env)
    val bf = new Param("bf")
    bf.setEnv(env)
    val wi = new Param("wi")
    wi.setEnv(env)
    val bi = new Param("bi")
    bi.setEnv(env)
    val wc = new Param("wc")
    wc.setEnv(env)
    val bc = new Param("bc")
    bc.setEnv(env)
    val wo = new Param("wo")
    wo.setEnv(env)
    val bo = new Param("bo")
    bo.setEnv(env)
    val x = new Input("x")
    x.setEnv(env)
    val h = new Input("h")
    h.setEnv(env)
    val c = new Input("c")
    c.setEnv(env)

    val cell = new LSTMCell(wf, bf, wi, bi, wc, bc, wo, bo, x, h, c)

    wf.setValue(Nd4j.create(Array(Array(1d, 2, 3, 1, 2, 2d), Array(2d, 0, 1, 2, 1, 0), Array(0d, 1, 1, 0, 1, 2)))
      .transposei().divi(100))
    bf.setValue(Nd4j.create(Array(3d, 0, 1)).divi(100))
    wi.setValue(Nd4j.create(Array(Array(2d, 1, 0, 1, 2, 2d), Array(3d, 0, 2, 2, 5, 0), Array(0d, 2, 1, 3, 1, 2)))
      .transposei.divi(100))
    bi.setValue(Nd4j.create(Array(1d, 2, 0)).divi(100))
    wc.setValue(Nd4j.create(Array(Array(2d, 2, 3, 2, 2, 2d), Array(2d, 1, 1, 2, 3, 0), Array(1d, 1, 1, 2, 0, 2)))
      .transposei.divi(100))
    bc.setValue(Nd4j.create(Array(1d, 1, 1)).divi(100))
    wo.setValue(Nd4j.create(Array(Array(2d, 2, 2, 1, 2, 1d), Array(1d, 1, 1, 1, 1, 0), Array(0d, 0, 1, 0, 1, 0)))
      .transposei.divi(100))
    bo.setValue(Nd4j.create(Array(1d, 0, 2)).divi(100))

    x.setValue(Nd4j.create(Array(Array(1d, 0, 1), Array(1d, 1, 0))))
    h.setValue(Nd4j.zeros(2, 3))
    c.setValue(Nd4j.ones(2, 3))

    env.forward

    val hout = cell.hout.value
    val cout = cell.cout.value

    val cexpect = Nd4j.create(Array(Array(0.5405, 0.5203, 0.5331), Array(0.5404, 0.5388, 0.5203)))
    val hexpect = Nd4j.create(Array(Array(0.2504, 0.2401, 0.2463), Array(0.2516, 0.2485, 0.2425)))

    assertArrayEquals(Array(2, 3), hout.shape)
    assertArrayEquals(Array(2, 3), cout.shape)

    for (i <- 0 to 1; j <- 0 to 2) {
      assertEquals(hexpect.getDouble(i, j), hout.getDouble(i, j), 0.001)
      assertEquals(cexpect.getDouble(i, j), cout.getDouble(i, j), 0.001)
    }
  }
}