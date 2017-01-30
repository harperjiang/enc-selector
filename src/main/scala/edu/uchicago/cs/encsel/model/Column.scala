package edu.uchicago.cs.encsel.model

import java.net.URI

class Column(o: URI, ci: Int, cn: String, dt: DataType) {
  var origin: URI = o
  var colIndex: Int = ci
  var colName: String = cn
  var colFile: URI = null
  var dataType = dt
}