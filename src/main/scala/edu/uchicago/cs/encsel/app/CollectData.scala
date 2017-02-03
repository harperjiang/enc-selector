package edu.uchicago.cs.encsel.app;

import java.io.File

import edu.uchicago.cs.encsel.datacol.DataCollector

object CollectData extends App {
  var f = new File(args(0))
  new DataCollector().scan(f.toURI())
}
