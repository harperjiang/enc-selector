package edu.uchicago.cs.encsel

import edu.uchicago.cs.encsel.datacol.DataCollector
import java.net.URI

object Main extends App {
  new DataCollector().collect(new URI("file", null, args(0), null))
}