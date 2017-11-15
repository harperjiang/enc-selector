package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.converter.RowConverter
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.VersionParser
import org.apache.parquet.column.impl.ColumnReaderImpl
import org.apache.parquet.column.page.{DataPage, PageReadStore}
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.hadoop.util.HiddenFileFilter

import scala.collection.JavaConversions._

object HorizontalScan extends App {
  val schema = TPCHSchema.customerSchema
  val inputFolder = "/Users/harper/TPCH/"
  val suffix = ".parquet"

  val file = new File("%s%s%s".format(inputFolder, schema.getName, suffix)).toURI
  val conf = new Configuration
  val path = new Path(file)
  val fs = path.getFileSystem(conf)
  val statuses = fs.listStatus(path, HiddenFileFilter.INSTANCE).toIterator.toList
  val footers = ParquetFileReader.readAllFootersInParallelUsingSummaryFiles(conf, statuses, false)
  if (footers.isEmpty)
    throw new IllegalArgumentException();

  for (footer <- footers) {
    val version = VersionParser.parse(footer.getParquetMetadata.getFileMetaData.getCreatedBy)
    val fileReader = ParquetFileReader.open(conf, footer.getFile, footer.getParquetMetadata)
    var blockCounter = 0
    val schema = footer.getParquetMetadata.getFileMetaData.getSchema
    val cols = schema.getColumns

    val recorder = new RowConverter(schema);
    var rowGroup: PageReadStore = null;
    var dataPage: DataPage = null

    rowGroup = fileReader.readNextRowGroup()
    while (rowGroup != null) {
      val blockMeta = footer.getParquetMetadata.getBlocks.get(blockCounter)
      val readers = cols.zipWithIndex.map(col => {
        new ColumnReaderImpl(col._1, rowGroup.getPageReader(col._1), recorder.getConverter(col._2).asPrimitiveConverter(), version)
      })
      // Predicate on one column
      val priceReader = readers(5)
      var counter = 0;

      while (counter < blockMeta.getRowCount) {
        val predicateOn = priceReader.getDouble;

        if (predicateOn < 5000) {
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

      blockCounter += 1
      rowGroup = fileReader.readNextRowGroup()
    }
  }
}
