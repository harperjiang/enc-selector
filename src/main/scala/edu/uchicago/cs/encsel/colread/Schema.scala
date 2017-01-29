package edu.uchicago.cs.encsel.colread

import edu.uchicago.cs.encsel.model.DataType
import java.net.URI
import scala.io.Source
import java.util.regex.Pattern
import java.util.regex.Matcher

class Schema {

  var columns: Array[(DataType, String)] = null;

  def this(columns: Array[(DataType, String)]) {
    this()
    this.columns = columns
  }

}

object Schema {
  def fromParquetFile(file: URI): Schema = {
    return new Schema(Source.fromFile(file).getLines().collect(parseParquetLine()).toArray)
  }

  private val pattern = "^\\s*(?:required|optional)\\s+([\\d\\w]+)\\s+([\\d\\w_]+)\\s*;\\s*$".r

  private def parseParquetLine(): PartialFunction[String, (DataType, String)] = {
    case pattern(a, b) => (dataType(a), b)
  }

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
