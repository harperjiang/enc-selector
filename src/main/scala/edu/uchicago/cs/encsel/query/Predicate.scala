package edu.uchicago.cs.encsel.query

import org.apache.parquet.column.ColumnReader
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName

trait Predicate {
  def test(): Boolean
}

class ColumnPredicate[T](predicate: (T) => Boolean) extends Predicate {

  var column: ColumnReader = null

  def setColumn(column: ColumnReader) = this.column = column

  def test(): Boolean = {
    column.getDescriptor.getType match {
      case PrimitiveTypeName.DOUBLE => predicate(column.getDouble.asInstanceOf[T])
      case PrimitiveTypeName.BINARY => predicate(column.getBinary.asInstanceOf[T])
      case PrimitiveTypeName.INT32 => predicate(column.getInteger.asInstanceOf[T])
      case PrimitiveTypeName.INT64 => predicate(column.getLong.asInstanceOf[T])
      case PrimitiveTypeName.FLOAT => predicate(column.getFloat.asInstanceOf[T])
      case PrimitiveTypeName.BOOLEAN => predicate(column.getBoolean.asInstanceOf[T])
      case _ => throw new IllegalArgumentException
    }
  }
}

class AndPredicate(p1: Predicate, p2: Predicate) extends Predicate {
  override def test(): Boolean = p1.test() && p2.test()
}
