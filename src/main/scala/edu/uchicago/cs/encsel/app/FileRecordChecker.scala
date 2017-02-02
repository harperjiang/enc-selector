package edu.uchicago.cs.encsel.app

import java.io.File
import java.io.FileReader

import scala.collection.JavaConversions._

import org.apache.commons.csv.CSVFormat

object FileRecordChecker extends App {

  var parser = CSVFormat.EXCEL.withFirstRecordAsHeader().parse(new FileReader(new File("food_insp.csv")))

  parser.iterator().foreach(rec => {
    if (rec.size() != 16) {
      System.out.println(rec.size())
    }
    if (rec.get("Zip").equals("IL")) {
      System.out.println(rec.iterator().mkString("$$"))
    }
  })
}

