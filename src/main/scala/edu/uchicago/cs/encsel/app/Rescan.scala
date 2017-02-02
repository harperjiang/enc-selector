package edu.uchicago.cs.encsel.app

import java.io.File
import edu.uchicago.cs.encsel.datacol.Persistence
import edu.uchicago.cs.encsel.model.Column
import edu.uchicago.cs.encsel.datacol.DataCollector

object Rescan extends App {

  if (args.length == 0) {
    System.out.println("This application rescans a file and replace entries that already exist")
    System.exit(0)
  }

  var target = new File(args(0)).toURI()

  var datas = Persistence.get.load()
  var filtered = datas.filter { !_.origin.equals(target) }.toList

  System.out.println("Found and remove %d entries".format(datas.size - filtered.size))
  Persistence.get.clean()
  Persistence.get.save(filtered)

  new DataCollector().collect(target)
}