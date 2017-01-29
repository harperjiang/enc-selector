package edu.uchicago.cs.encsel.model

import java.net.URI
import edu.uchicago.cs.encsel.feature.Feature

class Data {

  var origin: URI = null
  var originCol: Int = -1
  var name: String = null;

  var dataType = DataType.STRING;
  var encoding: String = null
  var features = Iterable[Feature]()
}