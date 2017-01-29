package edu.uchicago.cs.encsel.csv

import scala.io.Source
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import java.io.File
import java.net.URI

class CSVParser {

  def parse(inputFile: URI): Iterator[Array[String]] = {
    return Source.fromFile(inputFile).getLines().map { parseLine(_) }
  }

  def parse(inputString: String): Iterator[Array[String]] = {
    return inputString.split("[\r\n]+").map { parseLine(_) }.toIterator
  }

  def parseLine(line: String): Array[String] = {
    var content = new ArrayBuffer[String]();
    var buffer = new StringBuffer();
    var state = 0 // 0 is field start, 1 is in string, 2 is in field, 3 is string end
    line.foreach { c =>
      {
        state match {
          case 0 => {
            c match {
              case '\"' => { state = 1 }
              case ',' => { content += buffer.toString(); buffer.delete(0, buffer.length()) }
              case _ => { state = 2; buffer.append(c) }
            }
          }
          case 1 => {
            c match {
              case '\"' => { state = 3 }
              case _ => { buffer.append(c) }
            }
          }
          case 2 => {
            c match {
              case ',' => { content += buffer.toString(); buffer.delete(0, buffer.length()); state = 0 }
              case _ => { buffer.append(c) }
            }
          }
          case 3 => {
            c match {
              case ',' => { content += buffer.toString(); buffer.delete(0, buffer.length()); state = 0 }
              case _ => throw new IllegalArgumentException()
            }
          }
          case _ => throw new IllegalArgumentException()
        }
      }
    }
    if (state == 2 || state == 3) {
      content += buffer.toString()
    }
    return content.toArray
  }
}