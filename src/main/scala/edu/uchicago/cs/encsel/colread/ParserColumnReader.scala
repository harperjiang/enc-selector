package edu.uchicago.cs.encsel.colread

import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.File
import edu.uchicago.cs.encsel.model.Column
import java.net.URI

class ParserColumnReader(p: Parser) extends ColumnReader {
  var parser = p

  def readColumn(source: URI, schema: Schema): Iterable[Column] = {
    var tempFolder = allocTempFolder(source)
    var colWithWriter = schema.columns.zipWithIndex.map(d => {
      var col = new Column(source, d._2, d._1._2, d._1._1)
      col.colFile = allocFileForCol(tempFolder, d._1._2, d._2)
      var writer = new PrintWriter(new FileOutputStream(new File(col.colFile)))
      (col, writer)
    }).toArray

    var parsed = parser.parse(source, schema);

    if (schema.hasHeader)
      parsed = parsed.drop(1)
    parsed.foreach { row =>
      {
        if (row.length > colWithWriter.size)
          throw new IllegalArgumentException("Row size exceed schema length: " + row.mkString("$$"))
        row.zipWithIndex.foreach(col => {
          colWithWriter(col._2)._2.println(col._1)
        })
      }
    }
    colWithWriter.foreach(t => { t._2.close })
    return colWithWriter.map(_._1)
  }
}