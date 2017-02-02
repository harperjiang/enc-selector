package edu.uchicago.cs.encsel

import java.util.Properties

object Config {

  load()

  var columnReaderEnableCheck = true

  def load(): Unit = {
    var p = new Properties()
    p.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("config.properties"))
    columnReaderEnableCheck = "true".equals(p.getProperty("columnreader.enablecheck"))
  }
}