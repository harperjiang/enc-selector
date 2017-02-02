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
  datas = datas.filter { !_.origin.equals(target) }
  Persistence.get.clean()
  Persistence.get.save(datas)

  new DataCollector().collect(target)
}