package edu.uchicago.cs.encsel.colread.csv

import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.PrintWriter
import java.net.URI

import scala.collection.JavaConversions.asScalaIterator

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.slf4j.LoggerFactory

import edu.uchicago.cs.encsel.Config
import edu.uchicago.cs.encsel.colread.ColumnReader
import edu.uchicago.cs.encsel.colread.Schema
import edu.uchicago.cs.encsel.model.Column

/**
 * This Column Reader use Apache Commons CSV Parser
 */
class CSVColumnReader2 extends ColumnReader {

  var logger = LoggerFactory.getLogger(getClass())

  def readColumn(source: URI, schema: Schema): Iterable[Column] = {
    var tempFolder = allocTempFolder(source)
    var colWithWriter = schema.columns.zipWithIndex.map(d => {
      var col = new Column(source, d._2, d._1._2, d._1._1)
      col.colFile = allocFileForCol(tempFolder, d._1._2, d._2)
      var writer = new PrintWriter(new FileOutputStream(new File(col.colFile)))
      (col, writer)
    }).toArray

    var parseFormat = CSVFormat.EXCEL
    if (schema.hasHeader)
      parseFormat = parseFormat.withFirstRecordAsHeader()
    var parser = parseFormat.parse(new FileReader(new File(source)))

    var iterator = parser.iterator()
    if (schema.hasHeader) {
      iterator.next()
    }
    iterator.foreach { record =>
      {
        if (!validate(record, schema)) {
          logger.warn("Malformated record at " + record.getRecordNumber + " found, skipping:" + record.toString)
        } else {
          record.iterator().zipWithIndex.foreach(rec => {
            colWithWriter(rec._2)._2.println(rec._1)
          })
        }
      }
    }
    colWithWriter.foreach(t => { t._2.close })
    return colWithWriter.map(_._1)
  }

  def validate(record: CSVRecord, schema: Schema): Boolean = {
    if (!Config.columnReaderEnableCheck)
      return true
    if (record.size() > schema.columns.size) {
      return false
    }
    schema.columns.zipWithIndex.foreach(col => {
      if (col._2 < record.size && !col._1._1.check(record.get(col._2)))
        return false
    })

    return true
  }
}