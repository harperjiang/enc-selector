package edu.uchicago.cs.encsel.colread

import edu.uchicago.cs.encsel.model.DataType
import java.net.URI
import scala.io.Source
import java.util.regex.Pattern
import java.util.regex.Matcher
import scala.collection.mutable.ArrayBuffer

class Schema {

  var hasHeader = false

  var columns: Array[(DataType, String)] = null;

  def this(columns: Array[(DataType, String)], hasheader: Boolean = true) {
    this()
    this.columns = columns
    this.hasHeader = hasheader
  }

}

object Schema {
  def fromParquetFile(file: URI): Schema = {
    var hasheader = true
    var cols = new ArrayBuffer[(DataType, String)]()
    Source.fromFile(file).getLines().foreach {
      _ match {
        case hasheaderp(_*) => { hasheader = true }
        case noheaderp(_*) => { hasheader = false }
        case pattern(a, b) => { cols += ((dataType(a), b)) }
        case _ => {}
      }
    }

    return new Schema(cols.toArray, hasheader)
  }

  private val pattern = "^\\s*(?:required|optional)\\s+([\\d\\w]+)\\s+([\\d\\w_]+)\\s*;\\s*$".r
  private val hasheaderp = "^\\s*has_header\\s*$".r
  private val noheaderp = "^\\s*no_header\\s*$".r

  private def dataType(parquetType: String): DataType = {
    parquetType match {
      case "int32" => DataType.INTEGER
      case "int64" => DataType.LONG
      case "binary" => DataType.STRING
      case "double" => DataType.FLOAT
      case "float" => DataType.FLOAT
      case "boolean" => DataType.BOOLEAN
      case _ => throw new IllegalArgumentException(parquetType)
    }
  }
}
