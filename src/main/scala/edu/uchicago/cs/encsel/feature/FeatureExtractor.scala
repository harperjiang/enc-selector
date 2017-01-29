package edu.uchicago.cs.encsel.feature

import java.io.File
import java.net.URI

trait FeatureExtractor {
  def extract(input: URI): Iterable[Feature]
}