package edu.uchicago.cs.encsel.datacollect

import edu.uchicago.cs.encsel.model.DataType

class Record {
  var csvFileName: String = null
  var colFileName: String = null
  var colIndex: Int = -1
  var dataType = DataType.STRING
}