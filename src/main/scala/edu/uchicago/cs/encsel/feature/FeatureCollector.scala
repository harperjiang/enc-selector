package edu.uchicago.cs.encsel.feature

import edu.uchicago.cs.encsel.datacol.persist.FilePersistence

class FeatureCollector {

  var persistence = new FilePersistence()

  def collect(): Unit = {
    var cols = persistence.load()
    cols.foreach { col => col.features = Features.extract(col) }
    persistence.save(cols)
  }
}