package edu.uchicago.cs.encsel.colread.csv

import scala.collection.JavaConversions._

import edu.uchicago.cs.encsel.colread.ColumnReader
import edu.uchicago.cs.encsel.colread.Schema
import edu.uchicago.cs.encsel.model.Column
import java.net.URI
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.nio.charset.Charset
import org.apache.commons.csv.CSVFormat
import java.io.FileReader
import org.slf4j.LoggerFactory

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

    var parser = CSVFormat.EXCEL.parse(new FileReader(new File(source)))

    //    if (schema.hasHeader)
    //      parsed = parsed.drop(1)
    parser.iterator().foreach { record =>
      {
        if (record.size() != colWithWriter.size) {
          logger.warn("Malformated record at " + record.getRecordNumber + " found, ignoring:" + record.toString)
        } else {
          record.iterator().zipWithIndex.foreach(col => {
            colWithWriter(col._2)._2.println(col._1)
          })
        }
      }
    }
    colWithWriter.foreach(t => { t._2.close })
    return colWithWriter.map(_._1)
  }

}