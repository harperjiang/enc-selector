package edu.uchicago.cs.encsel.colread

import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.net.URI

import edu.uchicago.cs.encsel.csv.CSVParser
import edu.uchicago.cs.encsel.model.Column

class CSVColumnReader extends ColumnReader {
  var csvParser = new CSVParser

  def readColumn(source: URI, schema: Schema): Iterable[Column] = {
    var tempFolder = allocTempFolder(source)
    var colWithWriter = schema.columns.zipWithIndex.map(d => {
      var col = new Column(source, d._2, d._1._2, d._1._1)
      col.colFile = allocFileForCol(tempFolder, d._1._2, d._2)
      var writer = new PrintWriter(new FileOutputStream(new File(col.colFile)))
      (col, writer)
    }).toArray

    csvParser.parse(source).foreach { row =>
      {
        row.zipWithIndex.foreach(col => {
          colWithWriter(col._2)._2.println(col._1)
        })
      }
    }
    colWithWriter.foreach(t => { t._2.close })
    return colWithWriter.map(_._1)
  }
}