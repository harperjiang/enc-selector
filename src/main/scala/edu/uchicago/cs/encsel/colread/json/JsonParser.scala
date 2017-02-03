package edu.uchicago.cs.encsel.colread.json

import edu.uchicago.cs.encsel.colread.Parser
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer
import com.google.gson.JsonPrimitive

/**
 * This Parser parse per-line json object format, which
 * is common when large files are being processed
 */
class JsonParser extends Parser {

  var jsonParser = new com.google.gson.JsonParser

  def parseLine(line: String): Array[String] = {
    var jsonObject = jsonParser.parse(line).getAsJsonObject
    schema.columns.map(f => {
      if (jsonObject.has(f._2))
        jsonObject.get(f._2) match {
          case x if x.isJsonPrimitive() => x.getAsString
          case x if x.isJsonArray() => x.toString()
          case x if x.isJsonNull() => ""
          case x if x.isJsonObject() => x.toString()
        }
      else ""
    }).toArray
  }
}