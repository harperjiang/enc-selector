package edu.uchicago.cs.encsel.app

import edu.uchicago.cs.encsel.datacol.persist.FilePersistence

object Query extends App {

  var name = args(0)

  var fp = new FilePersistence()
  fp.load().foreach { col =>
    {
      if (col.origin.toString().contains(name)) {
        System.out.println(col.colFile.toString())
        System.exit(0)
      }
    }
  }
}