package edu.uchicago.cs.encsel.colread

import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.apache.commons.lang3.event.EventListenerSupport
import org.slf4j.LoggerFactory

import edu.uchicago.cs.encsel.Config
import edu.uchicago.cs.encsel.colread.listener.ColumnReaderListener
import edu.uchicago.cs.encsel.model.Column
import edu.uchicago.cs.encsel.colread.listener.ColumnReaderEvent
import edu.uchicago.cs.encsel.colread.listener.FailureMonitor

trait ColumnReader {

  protected var logger = LoggerFactory.getLogger(getClass())
  protected var eventSupport = new EventListenerSupport(classOf[ColumnReaderListener])

  eventSupport.addListener(new FailureMonitor)
  
  def readColumn(source: URI, schema: Schema): Iterable[Column]

  protected def allocTempFolder(source: URI): Path = {
    var tempRoot = Paths.get(Config.columnFolder)
    var tempFolder = Files.createTempDirectory(tempRoot, "colreader")
    tempFolder
  }

  protected def allocFileForCol(folder: Path, colName: String, colIdx: Int): URI = {
    var path = Files.createTempFile(folder, "%s_%d".format(colName, colIdx), null)
    path.toUri()
  }

  protected def fireStart(source: URI) = eventSupport.fire().start(new ColumnReaderEvent(source))
  protected def fireReadRecord(source: URI) = eventSupport.fire().readRecord(new ColumnReaderEvent(source))
  protected def fireFailRecord(source: URI) = eventSupport.fire().failRecord(new ColumnReaderEvent(source))
  protected def fireDone(source: URI) = eventSupport.fire().done(new ColumnReaderEvent(source))
}