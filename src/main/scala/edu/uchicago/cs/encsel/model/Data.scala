package edu.uchicago.cs.encsel.model

import java.net.URI

import scala.Iterable

import edu.uchicago.cs.encsel.feature.Feature

class Data extends Serializable {

  var origin: URI = null
  var originCol: Int = -1
  var name: String = null;

  var dataType = DataType.STRING;
  var encoding: String = null
  var features = Iterable[Feature]()
}