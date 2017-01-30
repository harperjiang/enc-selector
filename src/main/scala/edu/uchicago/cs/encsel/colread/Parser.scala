package edu.uchicago.cs.encsel.colread

import scala.io.Source
import java.net.URI

trait Parser {

  var schema: Schema = null

  def parse(inputFile: URI, schema: Schema): Iterable[Array[String]] = {
    this.schema = schema
    return Source.fromFile(inputFile).getLines().filter(!_.trim().isEmpty()).map { parseLine(_) }.toIterable
  }

  def parse(inputString: String, schema: Schema): Iterable[Array[String]] = {
    this.schema = schema
    return inputString.split("[\r\n]+").map { parseLine(_) }.toIterable
  }

  def parseLine(line: String): Array[String]
}