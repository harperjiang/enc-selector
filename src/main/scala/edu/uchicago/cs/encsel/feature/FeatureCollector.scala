package edu.uchicago.cs.encsel.feature

import edu.uchicago.cs.encsel.datacol.Persistence

class FeatureCollector {

  var persistence = Persistence.get

  def collect(): Unit = {
    var cols = persistence.load()
    cols.foreach { col => col.features = Features.extract(col) }
    persistence.clean()
    persistence.save(cols)
  }
}