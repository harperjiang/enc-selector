package edu.uchicago.cs.encsel.colread

class ColumnReaderFactory {

  def getColumnReader(source: DataSource.Type): ColumnReader = {
    source match {
      case DataSource.CSV => return new CSVColumnReader()
    }
    throw new UnsupportedOperationException();
  }
}

object DataSource extends Enumeration {
  type Type = Value
  val CSV = Value
}