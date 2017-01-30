package edu.uchicago.cs.encsel.feature

import java.io.File
import java.net.URI
import edu.uchicago.cs.encsel.model.Column

trait FeatureExtractor {
  def extract(input: Column): Iterable[Feature]
}