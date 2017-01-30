package edu.uchicago.cs.encsel.colread

import edu.uchicago.cs.encsel.colread.csv.CSVColumnReader
import java.net.URI
import edu.uchicago.cs.encsel.colread.tsv.TSVColumnReader
import edu.uchicago.cs.encsel.colread.json.JsonColumnReader

object ColumnReaderFactory {

  def getColumnReader(source: URI): ColumnReader = {
    source.getScheme match {
      case "file" => {
        source.getPath match {
          case x if x.toLowerCase().endsWith("csv") => {
            new CSVColumnReader
          }
          case x if x.toLowerCase().endsWith("tsv") => {
            new TSVColumnReader
          }
          case x if x.toLowerCase().endsWith("json") => {
            new JsonColumnReader
          }
          case _ =>
            throw new UnsupportedOperationException();
        }
      }
      case _ =>
        throw new UnsupportedOperationException();
    }
  }
}

object DataSource extends Enumeration {
  type Type = Value
  val CSV, TSV = Value
}