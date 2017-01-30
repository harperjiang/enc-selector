package edu.uchicago.cs.encsel.datacol

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

import scala.Iterable
import scala.collection.JavaConversions.asScalaIterator

import edu.uchicago.cs.encsel.colread.ColumnReader
import edu.uchicago.cs.encsel.colread.ColumnReaderFactory
import edu.uchicago.cs.encsel.colread.DataSource
import edu.uchicago.cs.encsel.colread.Schema
import edu.uchicago.cs.encsel.datacol.persist.FilePersistence
import edu.uchicago.cs.encsel.feature.Features
import edu.uchicago.cs.encsel.model.Column
import edu.uchicago.cs.encsel.model.Data
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.encsel.model.FloatEncoding
import edu.uchicago.cs.encsel.model.IntEncoding
import edu.uchicago.cs.encsel.model.StringEncoding
import edu.uchicago.cs.encsel.parquet.ParquetWriterHelper

class DataCollector {
  val colReaderFactory = new ColumnReaderFactory()

  var persistence = new FilePersistence

  def collect(source: URI): Unit = {
    var target = Paths.get(source)
    if (Files.isDirectory(target)) {
      target.iterator().foreach { p => collect(p.toUri()) }
      return
    }
    if (isDone(source))
      return
    val defaultSchema = getSchema(source)
    if (null == defaultSchema)
      throw new IllegalArgumentException("Schema not found:" + source)
    var colreader: ColumnReader = getColumnReader(source)

    val columns = colreader.readColumn(source, defaultSchema)

    var datalist = columns.map(mapData(_))

    persistence.save(datalist)

    markDone(source)
  }

  protected def getColumnReader(source: URI) = {
    source.getScheme match {
      case "file" => {
        source.getPath match {
          case x if x.endsWith("csv") => {
            colReaderFactory.getColumnReader(DataSource.CSV)
          }
          case _ => throw new IllegalArgumentException("Unrecognized source:" + source)
        }
      }
      case _ => throw new IllegalArgumentException("Unrecognized source:" + source)
    }
  }

  protected def isDone(file: URI): Boolean = {
    return Files.exists(Paths.get(new URI("%s.done".format(file.toString()))))
  }

  protected def markDone(file: URI) = {
    Files.createFile(Paths.get(new URI("%s.done".format(file.toString()))))
  }

  private def mapData(col: Column): Data = {
    var data = new Data()
    data.dataType = col.dataType
    data.origin = col.origin
    data.originCol = col.colIndex
    data.name = col.colName
    data.features = Features.extract(col)
    data
  }

  protected def getSchema(source: URI): Schema = {
    var schemaUri = new URI(source.getScheme, source.getHost,
      "%s.schema".format(source.getPath), null)
    if (new File(schemaUri).exists) {
      return Schema.fromParquetFile(schemaUri)
    }
    schemaUri = new URI(source.getScheme, source.getHost,
      source.getPath.replaceAll("\\.[\\d\\w]+$", ".schema"), null)
    if (new File(schemaUri).exists) {
      return Schema.fromParquetFile(schemaUri)
    }
    return null
  }
}