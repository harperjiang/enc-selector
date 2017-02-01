package edu.uchicago.cs.encsel.colread

import java.net.URI

import edu.uchicago.cs.encsel.model.Column
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

trait ColumnReader {
  def readColumn(source: URI, schema: Schema): Iterable[Column]

  protected def allocTempFolder(source: URI): Path = {
    var tempRoot: Path = null;
    //    if (!System.getProperty("java.io.tmpdir").isEmpty())
    //      tempRoot = Paths.get(System.getProperty("java.io.tmpdir"))
    //    if (!Files.exists(tempRoot))
    tempRoot = Paths.get("./columns")
    var tempFolder = Files.createTempDirectory(tempRoot, "colreader")
    tempFolder
  }

  protected def allocFileForCol(folder: Path, colName: String, colIdx: Int): URI = {
    var path = Files.createTempFile(folder, "%s_%d".format(colName, colIdx), null)
    path.toUri()
  }
}