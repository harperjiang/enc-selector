package edu.uchicago.cs.encsel

import java.util.Properties
import java.io.FileNotFoundException
import org.slf4j.LoggerFactory

object Config {

  var columnReaderEnableCheck = true
  var columnFolder = "./columns"

  load()

  var logger = LoggerFactory.getLogger(getClass())

  def load(): Unit = {
    try {
      var p = new Properties()
      p.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("config.properties"))
      columnReaderEnableCheck = "true".equals(p.getProperty("column.reader.enablecheck"))
      columnFolder = p.getProperty("column.folder")
    } catch {
      case e: Exception => {
        logger.warn("Failed to load configuration", e)
      }
    }
  }
}