package edu.uchicago.cs.encsel.app

import edu.uchicago.cs.encsel.datacol.Persistence

/**
 * Clean data from storage
 */
object Clean extends App {

  var todelete = args(0)

  var persist = Persistence.get

  var data = persist.load()

  var filtered = data.filter { col =>
    {
      !col.origin.toString().contains(todelete)
    }
  }.toList

  persist.clean()
  persist.save(filtered)
}