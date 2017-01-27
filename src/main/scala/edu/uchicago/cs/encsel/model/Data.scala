package edu.uchicago.cs.encsel.model


object DataType extends Enumeration {
  type DataType = Value
  val INTEGER, FLOAT, STRING = Value
}

class Data {

  var origin: String = null
  var originCol: Int = -1

  var dataType = DataType.INTEGER;
  var encoded = Map[String, Int]()

}