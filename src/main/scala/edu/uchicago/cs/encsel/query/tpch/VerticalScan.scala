package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.query.{RowTempTable, VColumnPredicate}
import edu.uchicago.cs.encsel.query.operator.VerticalSelect


object VerticalScan extends App {

  val schema = TPCHSchema.lineitemSchema
  //  val inputFolder = "/Users/harper/TPCH/"
  val inputFolder = args(0)
  val colIndex = 5
  val suffix = ".parquet"
  val file = new File("%s%s%s".format(inputFolder, schema.getName, suffix)).toURI
  val recorder = new RowTempTable(schema)

  val thresholds = Array(6000, 8000, 17000, 36000, 50000, 53000, 63000, 69000)
  println(thresholds.map(scan(_)).mkString("\n"))

  def scan(threshold: Long): Long = {
    val predicate = new VColumnPredicate((value: Any) => value.asInstanceOf[Double] < threshold, colIndex)
    val start = System.currentTimeMillis()

    new VerticalSelect().select(file, predicate, schema, Array(0, 1, 2, 3, 4))

    System.currentTimeMillis() - start
  }
}
