package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.ParquetWriterHelper

object LoadTPCH extends App {

  val folder = "/Users/harper/TPCH/"
  val inputsuffix = ".tbl"
  val outputsuffix = ".parquet"

  // Load TPCH
  TPCHSchema.schemas.foreach(schema => {
    ParquetWriterHelper.write(
      new File("%s%s%s".format(folder, schema.getName, inputsuffix)).toURI,
      schema,
      new File("%s%s%s".format(folder, schema.getName, outputsuffix)).toURI, true, "\\|")
  })

}
