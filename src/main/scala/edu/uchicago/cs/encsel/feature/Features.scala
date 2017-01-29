package edu.uchicago.cs.encsel.feature

import java.io.File

import scala.collection.mutable.ArrayBuffer
import java.net.URI

object Features {

  var extractors = new ArrayBuffer[FeatureExtractor]()

  install(FileSize)

  def install(fe: FeatureExtractor) = {
    extractors += fe
  }

  def extract(input: URI): Iterable[Feature] = {
    extractors.map(_.extract(input)).flatten
  }
}