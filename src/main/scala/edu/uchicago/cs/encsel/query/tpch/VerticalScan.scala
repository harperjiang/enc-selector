package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.dataset.parquet.converter.RowConverter
import edu.uchicago.cs.encsel.query.{Bitmap, ColumnPredicate}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.VersionParser
import org.apache.parquet.VersionParser.ParsedVersion
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.{DataPage, PageReadStore}
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.hadoop.metadata.BlockMetaData
import org.apache.parquet.hadoop.util.HiddenFileFilter

import scala.collection.JavaConversions._


object VerticalScan extends App {

  val schema = TPCHSchema.lineitemSchema
//  val inputFolder = "/Users/harper/TPCH/"
  val inputFolder = args(0)
  val suffix = ".parquet"

  val file = new File("%s%s%s".format(inputFolder, schema.getName, suffix)).toURI
  val predicate = new ColumnPredicate[Double]((value: Double) => value < 5000)
  val recorder = new RowConverter(schema)

  val start = System.currentTimeMillis()

  ParquetReaderHelper.read(file, new ReaderProcessor {
    override def processRowGroup(version: ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore): Unit = {
      val columns = schema.getColumns.zipWithIndex.map(col => new ColumnReaderImpl(col._1,
        rowGroup.getPageReader(col._1), recorder.getConverter(col._2).asPrimitiveConverter(), version))
      predicate.setColumn(columns(5))
      val bitmap = new Bitmap(rowGroup.getRowCount)

      for (count <- 0 until rowGroup.getRowCount) {
        bitmap.set(count, predicate.test())
      }
      columns.filter(_ != columns(5)).foreach(col => {
        for (count <- 0 until rowGroup.getRowCount) {
          if(bitmap.test(count)) {
            col.writeCurrentValueToConverter()
          } else {
            col.skip()
          }
          col.consume()
        }
      })
    }
  })

  val consumed = System.currentTimeMillis() - start
  println(consumed)
}
