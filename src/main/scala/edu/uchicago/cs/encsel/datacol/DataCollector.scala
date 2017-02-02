package edu.uchicago.cs.encsel.datacol

import java.io.File
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

import scala.collection.JavaConversions.asScalaIterator

import org.slf4j.LoggerFactory

import edu.uchicago.cs.encsel.colread.ColumnReader
import edu.uchicago.cs.encsel.colread.ColumnReaderFactory
import edu.uchicago.cs.encsel.colread.Schema
import edu.uchicago.cs.encsel.datacol.persist.FilePersistence
import edu.uchicago.cs.encsel.feature.Features
import edu.uchicago.cs.encsel.model.Column

class DataCollector {

  var persistence = new FilePersistence

  var logger = LoggerFactory.getLogger(this.getClass)

  def collect(source: URI): Unit = {
    try {
      if (logger.isDebugEnabled())
        logger.debug("Scanning " + source.toString())
      var target = Paths.get(source)
      if (Files.isDirectory(target)) {
        Files.list(target).iterator.foreach { p => collect(p.toUri()) }
        return
      }
      if (isDone(source)) {
        if (logger.isDebugEnabled())
          logger.debug("Scanned mark found, skip")
        return
      }

      var colreader: ColumnReader = ColumnReaderFactory.getColumnReader(source)
      if (colreader == null) {
        if (logger.isDebugEnabled())
          logger.debug("No available reader found, skip")
        return
      }
      val defaultSchema = getSchema(source)
      if (null == defaultSchema) {
        if (logger.isDebugEnabled())
          logger.debug("Schema not found, skip")
        return
      }
      val columns = colreader.readColumn(source, defaultSchema)

      columns.foreach(extractFeature(_))

      persistence.save(columns)

      markDone(source)
    } catch {
      case e: Exception => {
        logger.error("Exception while scanning " + source.toString, e)
      }
    }
  }

  protected def isDone(file: URI): Boolean = {
    return Files.exists(Paths.get(new URI("%s.done".format(file.toString()))))
  }

  protected def markDone(file: URI) = {
    Files.createFile(Paths.get(new URI("%s.done".format(file.toString()))))
  }

  private def extractFeature(col: Column): Unit = {
    try {
      col.features = Features.extract(col)
    } catch {
      case e: Exception => {
        logger.warn("Exception while processing column:%s@%s".format(col.colName, col.origin), e)
      }
    }
  }

  protected def getSchema(source: URI): Schema = {
    // file_name + .schema
    var schemaUri = new URI(source.getScheme, source.getHost,
      "%s.schema".format(source.getPath), null)
    if (new File(schemaUri).exists) {
      return Schema.fromParquetFile(schemaUri)
    }
    // file_name.abc => file_name.schema
    schemaUri = new URI(source.getScheme, source.getHost,
      source.getPath.replaceAll("\\.[\\d\\w]+$", ".schema"), null)
    if (new File(schemaUri).exists) {
      return Schema.fromParquetFile(schemaUri)
    }
    // file_name starts with schema
    var path = Paths.get(source)
    var pathname = path.getFileName.toString
    var schemas = Files.list(path.getParent).iterator().filter {
      p =>
        {
          var pname = p.getFileName.toString
          pname.endsWith(".schema") && pathname.startsWith(pname.replace(".schema", ""))
        }
    }
    if (!schemas.isEmpty) {
      schemaUri = schemas.next().toUri()
      return Schema.fromParquetFile(schemaUri)
    }
    return null

  }
}