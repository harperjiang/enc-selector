package edu.uchicago.cs.encsel.colread

import scala.io.Source
import java.net.URI

trait Parser {
  def parse(inputFile: URI): Iterable[Array[String]] = {
    return Source.fromFile(inputFile).getLines().filter(!_.trim().isEmpty()).map { parseLine(_) }.toIterable
  }

  def parse(inputString: String): Iterable[Array[String]] = {
    return inputString.split("[\r\n]+").map { parseLine(_) }.toIterable
  }

  def parseLine(line: String): Array[String]
}