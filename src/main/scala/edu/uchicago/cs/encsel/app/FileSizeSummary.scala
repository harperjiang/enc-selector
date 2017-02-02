package edu.uchicago.cs.encsel.app

import edu.uchicago.cs.encsel.datacol.persist.FilePersistence
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.encsel.feature.EncFileSize
import scala.collection.mutable.ArrayBuffer

object FileSizeSummary extends App {

  var columns = new FilePersistence().load()

  var intres = new ArrayBuffer[String]()
  var strres = new ArrayBuffer[String]()
  columns.foreach { col =>
    {
      var res = "%s,%s".format(col.colName,
        col.features.filter { _.isInstanceOf[EncFileSize] }
          .map { f => (f.name, f.value) }.toList.sorted.map(p => p._2.toInt.toString()).mkString(","))
      col.dataType match {
        case DataType.INTEGER => {
          intres += res
        }
        case DataType.STRING => {
          strres += res
        }
        case _ => {}
      }
    }
  }
  System.out.println("Integer Records")
  System.out.println(intres.mkString("\n"))
  System.out.println("String Records")
  System.out.println(strres.mkString("\n"))
}