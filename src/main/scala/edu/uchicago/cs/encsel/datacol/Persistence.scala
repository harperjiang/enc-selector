package edu.uchicago.cs.encsel.datacol

import edu.uchicago.cs.encsel.model.Column
import edu.uchicago.cs.encsel.datacol.persist.FilePersistence

trait Persistence {
  def save(datalist: Iterable[Column])
  def load(): Iterable[Column]
  def clean()
}

object Persistence {
  private var impl = new FilePersistence
  def get: Persistence = impl
}