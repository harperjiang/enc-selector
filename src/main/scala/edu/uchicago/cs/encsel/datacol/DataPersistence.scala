package edu.uchicago.cs.encsel.datacol

import edu.uchicago.cs.encsel.model.Data

abstract class DataPersistence {
  def save(datalist: Iterable[Data])
  def load(): Iterable[Data]
}