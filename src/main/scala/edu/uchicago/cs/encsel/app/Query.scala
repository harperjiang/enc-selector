package edu.uchicago.cs.encsel.app

import edu.uchicago.cs.encsel.datacol.persist.FilePersistence

object Query extends App {

  var name = "trip_data"

  var fp = new FilePersistence()
  fp.load().foreach { col =>
    {
      System.out.println(col.origin.toString())
      //      
      //      if (col.origin.toString().contains(name)) {
      //        System.out.println(col.colFile.toString())
      //        System.exit(0)
      //      }
    }
  }
}