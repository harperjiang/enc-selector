package edu.uchicago.cs.encsel.colread.listener

import org.slf4j.LoggerFactory

class FailureMonitor extends ColumnReaderListener {
  val FACTOR = 100
  var failedCount = 0
  var totalCount = 0
  var logger = LoggerFactory.getLogger(getClass())

  def start(event: ColumnReaderEvent): Unit = {
    failedCount = 0
    totalCount = 0
  }

  def readRecord(event: ColumnReaderEvent): Unit = {
    totalCount += 1
  }

  def failRecord(event: ColumnReaderEvent): Unit = {
    failedCount += 1
  }

  def done(event: ColumnReaderEvent): Unit = {
    if (failedCount * FACTOR > totalCount) {
      logger.warn("File contains too many malformatted records:" + event.getSource.toString)
    }
  }

}