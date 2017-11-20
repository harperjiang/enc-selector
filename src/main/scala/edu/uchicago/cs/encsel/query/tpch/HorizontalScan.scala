package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper
import edu.uchicago.cs.encsel.dataset.parquet.ParquetReaderHelper.ReaderProcessor
import edu.uchicago.cs.encsel.dataset.parquet.converter.RowConverter
import edu.uchicago.cs.encsel.query.ColumnPredicate
import org.apache.parquet.VersionParser.ParsedVersion
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.PageReadStore
import org.apache.parquet.hadoop.Footer
import org.apache.parquet.hadoop.metadata.BlockMetaData

import scala.collection.JavaConversions._

object HorizontalScan extends App {
  val schema = TPCHSchema.lineitemSchema
  //  val inputFolder = "/home/harper/TPCH/"
  val inputFolder = args(0)
  val colIndex = 5
  val suffix = ".parquet"
  val file = new File("%s%s%s".format(inputFolder, schema.getName, suffix)).toURI

  val recorder = new RowConverter(schema);

  val thresholds = Array(6000, 8000, 17000, 36000, 50000, 53000, 63000, 69000)
  println(thresholds.map(scan(_)).mkString("\n"))

  def scan(threshold: Long): Long = {
    val predicate = new ColumnPredicate[Double]((value: Double) => value < threshold)

    val start = System.currentTimeMillis()

    ParquetReaderHelper.read(file, new ReaderProcessor() {
      override def processFooter(footer: Footer): Unit = {}

      override def processRowGroup(version: ParsedVersion, meta: BlockMetaData, rowGroup: PageReadStore): Unit = {
        recorder.getRecords.clear()
        val cols = schema.getColumns

        val readers = cols.zipWithIndex.map(col => {
          new ColumnReaderImpl(col._1, rowGroup.getPageReader(col._1), recorder.getConverter(col._2).asPrimitiveConverter(), version)
        })

        // Predicate on column
        var counter = 0;

        val predicateReader = readers(colIndex)
        predicate.setColumn(predicateReader)

        while (counter < meta.getRowCount) {
          if (predicate.test()) {
//            recorder.start()
            readers.filter(_ != predicateReader).foreach(reader => {
//              reader.writeCurrentValueToConverter()
              reader.readValue()
              reader.consume()
            })
//            recorder.end()
          } else {
            readers.filter(_ != predicateReader).foreach(reader => {
              reader.skip()
              reader.consume()
            })
          }
          predicateReader.consume()
          counter += 1
        }
      }
    })

    System.currentTimeMillis() - start
  }
}
