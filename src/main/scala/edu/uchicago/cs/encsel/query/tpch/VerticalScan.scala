package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.dataset.parquet.converter.RowConverter
import edu.uchicago.cs.encsel.query.{Bitmap, ColumnPredicate}
import org.apache.parquet.VersionParser.ParsedVersion
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.hadoop.Footer
import org.apache.parquet.hadoop.metadata.BlockMetaData

import scala.collection.JavaConversions._


object VerticalScan extends App {

  val schema = TPCHSchema.lineitemSchema
  //  val inputFolder = "/Users/harper/TPCH/"
  val inputFolder = args(0)
  val colIndex = 5
  val suffix = ".parquet"
  val file = new File("%s%s%s".format(inputFolder, schema.getName, suffix)).toURI
  val recorder = new RowConverter(schema)

  val thresholds = Array(6000, 8000, 17000, 36000, 50000, 53000, 63000, 69000)
  println(thresholds.map(scan(_)).mkString("\n"))

  def scan(threshold: Long): Long = {
    val predicate = new ColumnPredicate[Double]((value: Double) => value < threshold)
    val start = System.currentTimeMillis()

    ParquetReaderHelper.read(file, new ReaderProcessor {
      override def processFooter(footer: Footer): Unit = {}

      override def processRowGroup(version: ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore): Unit = {
        val columns = schema.getColumns.zipWithIndex.map(col => new ColumnReaderImpl(col._1,
          rowGroup.getPageReader(col._1), recorder.getConverter(col._2).asPrimitiveConverter(), version))
        val predicateReader = columns(colIndex)
        predicate.setColumn(predicateReader)
        val bitmap = new Bitmap(rowGroup.getRowCount)

        for (count <- 0L until rowGroup.getRowCount) {
          bitmap.set(count, predicate.test())
        }
        columns.filter(_ != predicateReader).foreach(col => {
          for (count <- 0L until rowGroup.getRowCount) {
            if (bitmap.test(count)) {
              col.readValue()
            } else {
              col.skip()
            }
            col.consume()
          }
        })
      }
    })

    System.currentTimeMillis() - start
  }
}
