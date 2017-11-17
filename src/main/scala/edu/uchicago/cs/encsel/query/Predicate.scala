package edu.uchicago.cs.encsel.query

import org.apache.parquet.column.ColumnReader
import org.apache.parquet.schema.MessageType

trait Predicate {
  def test(): Boolean
}

class ColumnPredicate[T](predicate: (T) => Boolean) extends Predicate {

  var column: ColumnReader = null

  def setColumn(column: ColumnReader) = this.column = column

  override def test(): Boolean = {
    Class[T] match {
      case Double => predicate(column.getDouble)
      case String => predicate(column.getBinary)
      case Int => predicate(column.getInteger)
      case Long => predicate(column.getLong)
      case Float => predicate(column.getFloat)
      case Boolean => predicate(column.getBoolean)
      case _ => throw new IllegalArgumentException
    }
  }
}

class AndPredicate(p1: Predicate, p2: Predicate) extends Predicate {
  override def test(): Boolean = p1.test() && p2.test()
}
