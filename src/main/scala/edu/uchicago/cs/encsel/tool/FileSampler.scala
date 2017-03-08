package edu.uchicago.cs.encsel.tool

import scala.io.Source
import scala.util.Random
import java.io.FileOutputStream
import java.io.PrintWriter

object FileSampler extends App {

  val file = "/home/harper/enc_workspace/date.tmp"
  val train = "/home/harper/enc_workspace/train.txt"
  val test = "/home/harper/enc_workspace/test.txt"
  val trainRate = 0.01
  val testRate = 0.001

  val trainOut = new PrintWriter(new FileOutputStream(train))
  val testOut = new PrintWriter(new FileOutputStream(test))
  Source.fromFile(file).getLines.foreach(line => {
    val rand = Random.nextDouble()
    if (rand < trainRate)
      trainOut.println(line)
    if (rand < testRate)
      testOut.println(line)
  })

  trainOut.close
  testOut.close
}