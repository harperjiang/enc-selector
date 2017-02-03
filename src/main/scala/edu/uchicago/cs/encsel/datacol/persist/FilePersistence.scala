package edu.uchicago.cs.encsel.datacol.persist

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

import scala.Iterable
import scala.collection.mutable.ArrayBuffer

import edu.uchicago.cs.encsel.datacol.Persistence
import edu.uchicago.cs.encsel.model.Column

class FilePersistence extends Persistence {

  var storage = new File("storage.dat")
  var datalist: ArrayBuffer[Column] = new ArrayBuffer[Column]()

  def save(datalist: Iterable[Column]) = {
    this.datalist ++= datalist

    var objwriter = new ObjectOutputStream(new FileOutputStream(storage))
    objwriter.writeObject(this.datalist)
    objwriter.close()
  }

  def clean() = {
    this.datalist.clear()

    var objwriter = new ObjectOutputStream(new FileOutputStream(storage))
    objwriter.writeObject(this.datalist)
    objwriter.close()
  }

  def load(): Iterable[Column] = {
    try {
      if (null == datalist || datalist.isEmpty) {
        var objreader = new ObjectInputStream(new FileInputStream(storage))
        datalist = objreader.readObject().asInstanceOf[ArrayBuffer[Column]]
        objreader.close()
      }
      return datalist.clone()
    } catch {
      case e: Exception => {
        return Iterable[Column]()
      }
    }
  }
}