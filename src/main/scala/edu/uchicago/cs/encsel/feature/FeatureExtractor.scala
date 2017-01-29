package edu.uchicago.cs.encsel.feature

import java.io.File

trait FeatureExtractor {
  def extract(input: File): Iterable[Feature]
}