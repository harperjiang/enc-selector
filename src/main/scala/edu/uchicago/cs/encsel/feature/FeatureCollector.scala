package edu.uchicago.cs.encsel.feature

import edu.uchicago.cs.encsel.datacol.Persistence
import java.util.concurrent.Executors
import edu.uchicago.cs.encsel.Config

import edu.uchicago.cs.encsel.common.Conversions._

class FeatureCollector {

  var persistence = Persistence.get
  var threadPool = Executors.newFixedThreadPool(Config.collectorThreadCount)

  def collect(): Unit = {
    var cols = persistence.load()
    cols.foreach { col => col.features = Features.extract(col) }
    persistence.clean()
    persistence.save(cols)
  }
}