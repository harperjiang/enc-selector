package edu.uchicago.cs.encsel.feature

import java.io.File

import scala.collection.mutable.ArrayBuffer
import java.net.URI
import edu.uchicago.cs.encsel.model.Column

object Features {

  var extractors = new ArrayBuffer[FeatureExtractor]()

  install(EncFileSize)

  def install(fe: FeatureExtractor) = {
    extractors += fe
  }

  def extract(input: Column): Iterable[Feature] = {
    extractors.map(_.extract(input)).flatten
  }
}