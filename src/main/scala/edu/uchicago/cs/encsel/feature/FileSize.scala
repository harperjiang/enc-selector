package edu.uchicago.cs.encsel.feature

import java.io.File
import java.net.URI

class FileSize(f: File) extends Feature {
  var size = f.length

  def name = "file_size"
  def value = size
}

object FileSize extends FeatureExtractor {

  def extract(input: URI): Iterable[Feature] = {
    return List(new FileSize(new File(input)))
  }
}