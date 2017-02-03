package edu.uchicago.cs.encsel

import java.util.Properties
import java.io.FileNotFoundException
import org.slf4j.LoggerFactory

object Config {

  var collectorThreadCount = 10
  
  var columnReaderEnableCheck = true
  var columnFolder = "./columns"

  load()

  var logger = LoggerFactory.getLogger(getClass())

  def load(): Unit = {
    try {
      var p = new Properties()
      p.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("config.properties"))
      
      collectorThreadCount = Integer.parseInt(p.getProperty("collector.threadCount"))
      columnReaderEnableCheck = "true".equals(p.getProperty("column.readerEnableCheck"))
      columnFolder = p.getProperty("column.folder")
    } catch {
      case e: Exception => {
        logger.warn("Failed to load configuration", e)
      }
    }
  }
}