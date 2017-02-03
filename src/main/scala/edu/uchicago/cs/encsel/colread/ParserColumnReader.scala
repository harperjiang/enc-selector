package edu.uchicago.cs.encsel.colread

import java.io.PrintWriter
import java.io.FileOutputStream
import java.io.File
import edu.uchicago.cs.encsel.model.Column
import java.net.URI
import org.slf4j.LoggerFactory
import edu.uchicago.cs.encsel.Config

class ParserColumnReader(p: Parser) extends ColumnReader {
  var parser = p

  def readColumn(source: URI, schema: Schema): Iterable[Column] = {

    fireStart(source)

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
        fireReadRecord(source)
        if (!validate(row, schema)) {
          fireFailRecord(source)
          logger.warn("Malformated line found, ignoring:" + row.mkString("$<>$"))
        } else {
          row.zipWithIndex.foreach(col => {
            colWithWriter(col._2)._2.println(col._1)
          })
        }
      }
    }
    colWithWriter.foreach(t => { t._2.close })
    fireDone(source)

    return colWithWriter.map(_._1)
  }

  def validate(record: Array[String], schema: Schema): Boolean = {
    if (!Config.columnReaderEnableCheck)
      return true
    if (record.length > schema.columns.size) {
      return false
    }
    schema.columns.zipWithIndex.foreach(col => {
      if (col._2 < record.length && !col._1._1.check(record(col._2)))
        return false
    })

    return true
  }
}