package edu.uchicago.cs.encsel.colread

import scala.io.Source
import java.net.URI
import org.slf4j.LoggerFactory

trait Parser {

  var schema: Schema = null

  var logger = LoggerFactory.getLogger(getClass())

  def parse(inputFile: URI, schema: Schema): Iterable[Array[String]] = {
    this.schema = schema
    return Source.fromFile(inputFile).getLines().filter(!_.trim().isEmpty()).map { parseLineIgnoreError(_) }.toIterable
  }

  def parse(inputString: String, schema: Schema): Iterable[Array[String]] = {
    this.schema = schema
    return inputString.split("[\r\n]+").map { parseLineIgnoreError(_) }.toIterable
  }

  def parseLineIgnoreError(line: String): Array[String] = {
    try {
      return parseLine(line)
    } catch {
      case e: Exception => {
        logger.warn("Exception while parsing line:" + line, e)
        return Array[String]()
      }
    }
  }

  def parseLine(line: String): Array[String]
}