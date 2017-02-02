package edu.uchicago.cs.encsel.model

import java.net.URI

import scala.Iterable

import edu.uchicago.cs.encsel.feature.Feature

class Column(o: URI, ci: Int, cn: String, dt: DataType) extends Serializable {
  var origin: URI = o
  var colIndex: Int = ci
  var colName: String = cn
  var colFile: URI = null
  var dataType = dt
  var features = Iterable[Feature]()
}