package edu.uchicago.cs.encsel.feature

import java.io.File

import scala.collection.mutable.ArrayBuffer
import java.net.URI
import edu.uchicago.cs.encsel.model.Column
import org.slf4j.LoggerFactory

object Features {
  var logger = LoggerFactory.getLogger(getClass())
  var extractors = new ArrayBuffer[FeatureExtractor]()

  install(EncFileSize)

  def install(fe: FeatureExtractor) = {
    extractors += fe
  }

  def extract(input: Column): Iterable[Feature] = {
    extractors.flatMap(ex => {
      try {
        ex.extract(input)
      } catch {
        case e: Exception => {
          logger.error("Exception while executing %s, skipping".format(ex.getClass.getName), e)
          Iterable[Feature]()
        }
      }
    })
  }
}