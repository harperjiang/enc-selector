package edu.uchicago.cs.encsel.colread.listener

import java.util.EventListener
import java.util.EventObject
import java.net.URI

class ColumnReaderEvent(source: URI) extends EventObject(source);

trait ColumnReaderListener extends EventListener {
  def start(event: ColumnReaderEvent): Unit;
  def readRecord(event: ColumnReaderEvent): Unit;
  def failRecord(event: ColumnReaderEvent): Unit;
  def done(event: ColumnReaderEvent): Unit
}