package edu.uchicago.cs.encsel.app

import edu.uchicago.cs.encsel.datacol.persist.FilePersistence

object Query extends App {

  var name = args(0)

  var fp = new FilePersistence()
  fp.load().foreach { data =>
    {
      if (data.origin.toString().contains(name)) {
        System.out.println(data.originCol.toString)
        System.exit(0)
      }
    }
  }
}