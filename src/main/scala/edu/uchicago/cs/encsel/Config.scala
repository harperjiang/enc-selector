package edu.uchicago.cs.encsel

import java.util.Properties

object Config {

  var columnReaderEnableCheck = false

  load()

  def load(): Unit = {
    var p = new Properties()
    p.load(Thread.currentThread().getContextClassLoader.getResourceAsStream("config.properties"))
    columnReaderEnableCheck = "true".equals(p.getProperty("columnreader.enablecheck"))
  }
}