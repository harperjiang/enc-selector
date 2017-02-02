package edu.uchicago.cs.encsel.datacol

import edu.uchicago.cs.encsel.model.Column

trait Persistence {
  def save(datalist: Iterable[Column])
  def load(): Iterable[Column]
}