package edu.uchicago.cs.encsel.feature

import java.io.File

class FileSize(f: File) extends Feature {
  var size = f.length

  def name = "file_size"
  def value = size
}

object FileSize extends FeatureExtractor {

  def extract(input: File): Iterable[Feature] = {
    return List(new FileSize(input))
  }
}