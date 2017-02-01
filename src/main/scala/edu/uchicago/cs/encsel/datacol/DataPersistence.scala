package edu.uchicago.cs.encsel.datacol

import edu.uchicago.cs.encsel.model.Data

trait DataPersistence {
  def save(datalist: Iterable[Data])
  def load(): Iterable[Data]
}