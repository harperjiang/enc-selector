package edu.uchicago.cs.encsel.colread.tsv

import scala.io.Source
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer
import java.net.URI
import edu.uchicago.cs.encsel.colread.Parser

class TSVParser extends Parser {

  def parseLine(line: String): Array[String] = {
    return line.split("\t")
  }
}