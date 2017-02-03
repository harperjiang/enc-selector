package edu.uchicago.cs.encsel.colread

import java.net.URI

import edu.uchicago.cs.encsel.model.Column
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import edu.uchicago.cs.encsel.Config

trait ColumnReader {
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
}