package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.converter.RowConverter
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.VersionParser
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.{DataPage, PageReadStore}
import org.apache.parquet.hadoop.{Footer, ParquetFileReader}
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.query.{ColumnPredicate, Predicate}
import org.apache.parquet.VersionParser.ParsedVersion
import org.apache.parquet.column.ColumnReader
import org.apache.parquet.hadoop.metadata.BlockMetaData
import org.apache.parquet.hadoop.util.HiddenFileFilter
import org.apache.parquet.schema.MessageType

import scala.collection.JavaConversions._

object HorizontalScan extends App {
  val schema = TPCHSchema.lineitemSchema
//  val inputFolder = "/home/harper/TPCH/"
  val inputFolder = args(0)
  val suffix = ".parquet"
  val file = new File("%s%s%s".format(inputFolder, schema.getName, suffix)).toURI

  val recorder = new RowConverter(schema);

  val predicate = new ColumnPredicate[Double]((value: Double) => value < 5000)

  val start = System.currentTimeMillis()

  ParquetReaderHelper.read(file, new ReaderProcessor() {
    override def processFooter(footer: Footer): Unit = { }

    override def processRowGroup(version: ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore): Unit = {
      recorder.getRecords.clear()
      val cols = schema.getColumns

      val readers = cols.zipWithIndex.map(col => {
        new ColumnReaderImpl(col._1, rowGroup.getPageReader(col._1), recorder.getConverter(col._2).asPrimitiveConverter(), version)
      })

      // Predicate on column
      var counter = 0;

      val priceReader = readers(5)
      predicate.setColumn(readers(5))

      while (counter < meta.getRowCount) {
        if (predicate.test()) {
          recorder.start()
          readers.filter(_ != priceReader).foreach(reader => {
            reader.writeCurrentValueToConverter()
            reader.consume()
          })
          recorder.end()
        } else {
          readers.filter(_ != priceReader).foreach(reader => {
            reader.skip()
            reader.consume()
          })
        }
        priceReader.consume()
        counter += 1
      }
    }
  })

  val consumed = System.currentTimeMillis() - start
  println(consumed)
}
