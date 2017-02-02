package edu.uchicago.cs.encsel.app

import java.nio.file.Paths

import edu.uchicago.cs.encsel.datacol.Persistence

object List extends App {

  var fp = Persistence.get
  fp.load().foreach { col =>
    {
      var originName = Paths.get(col.origin).getFileName.toString()
      var colName = col.colName
      var colFile = col.colFile.toString()
      System.out.println("%s:%s=>%s".format(originName, colName, colFile))
    }
  }
}