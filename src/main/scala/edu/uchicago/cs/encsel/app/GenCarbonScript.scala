package edu.uchicago.cs.encsel.app

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

import scala.collection.JavaConversions._
import java.nio.file.Path
import java.nio.file.FileVisitOption

object GenCarbonScript extends App {

  //  println(folderSize(Paths.get(new File("/home/harper/Repositories/incubator-carbondata/bin/carbonshellstore/default/event_time_57463913727907242513").toURI())))

  compareSize

  def carbonScript: Unit = {
    var dir = Paths.get(new File("/home/harper/dataset/carbon").toURI())

    Files.list(dir).iterator().toArray.foreach(file => {
      val filename = file.getFileName.toString.replaceAll("\\.tmp$", "")
      println("""cc.sql("CREATE TABLE IF NOT EXISTS %s(id string) STORED BY 'carbondata'")""".format(filename))
      println("""cc.sql("LOAD DATA INPATH '%s' INTO TABLE %s")""".format(file.toString, filename))
    })
  }

  def addHeader: Unit = {

  }

  def compareSize: Unit = {
    var dir = Paths.get(new File("/home/harper/dataset/carbon").toURI())
    var carbondir = Paths.get(new File("/home/harper/Repositories/incubator-carbondata/bin/carbonshellstore/default").toURI)
    Files.list(dir).iterator().toArray.foreach(file => {
      val filename = file.getFileName.toString.replaceAll("\\.tmp$", "").toLowerCase()
      val filesize = new File(file.toUri()).length()
      val folder = carbondir.resolve(filename)
      if (Files.exists(folder)) {
        val foldersize = folderSize(folder)
        println("%s,%d,%d".format(filename, filesize, foldersize))
      }
    })
  }

  def folderSize(folder: Path): Long = {
    Files.walk(folder).iterator().filter { !Files.isDirectory(_) }.map(p => new File(p.toUri).length()).sum
  }
}