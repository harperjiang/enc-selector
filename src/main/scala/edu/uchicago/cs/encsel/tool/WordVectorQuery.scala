package edu.uchicago.cs.encsel.tool

import scala.io.Source

object WordVectorQuery extends App {

  val src = "/home/harper/Downloads/glove.42B.300d.txt"

  val word1 = "st."
  val word2 = "rd."
  val word3 = "road"
  val word4 = "street"
  val word5 = "avenue"

  Source.fromFile(src).getLines().foreach {
    line =>
      {
        val sp = line.split("\\s+")
        if (sp(0).equals(word1))
          println(line)
        if (sp(0).equals(word2))
          println(line)
        if (sp(0).equals(word3))
          println(line)
        if (sp(0).equals(word4))
          println(line)
        if (sp(0).equals(word5))
          println(line)
      }
  }
}