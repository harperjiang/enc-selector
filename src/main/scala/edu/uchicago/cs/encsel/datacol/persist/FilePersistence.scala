package edu.uchicago.cs.encsel.datacol.persist

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import scala.Iterable

import edu.uchicago.cs.encsel.datacol.Persistence
import edu.uchicago.cs.encsel.model.Column

class FilePersistence extends Persistence {

  var storage = new File("storage.dat")
  var datalist: Iterable[Column] = load()

  def save(datalist: Iterable[Column]) = {
    this.datalist = datalist

    var objwriter = new ObjectOutputStream(new FileOutputStream(storage))
    objwriter.writeObject(this.datalist)
    objwriter.close()
  }

  def load(): Iterable[Column] = {
    try {
      if (null == datalist) {
        var objreader = new ObjectInputStream(new FileInputStream(storage))
        datalist = objreader.readObject().asInstanceOf[Iterable[Column]]
        objreader.close()
      }
      return datalist
    } catch {
      case e: Exception => {
        return Iterable[Column]()
      }
    }
  }
}