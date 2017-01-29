package edu.uchicago.cs.encsel.datacol.persist

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import edu.uchicago.cs.encsel.datacol.DataPersistence
import edu.uchicago.cs.encsel.model.Data
import java.io.IOException

class FilePersistence extends DataPersistence {

  var storage = new File("storage.dat")
  var datalist: Iterable[Data] = load()

  def save(datalist: Iterable[Data]) = {
    this.datalist ++= datalist

    var objwriter = new ObjectOutputStream(new FileOutputStream(storage))
    objwriter.writeObject(this.datalist)
    objwriter.close()
  }

  def load(): Iterable[Data] = {
    try {
      if (null == datalist) {
        var objreader = new ObjectInputStream(new FileInputStream(storage))
        datalist = objreader.readObject().asInstanceOf[Iterable[Data]]
        objreader.close()
      }
      return datalist
    } catch {
      case e: Exception => {
        return Iterable[Data]()
      }
    }
  }
}