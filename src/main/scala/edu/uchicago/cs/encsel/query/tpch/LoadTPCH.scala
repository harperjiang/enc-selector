package edu.uchicago.cs.encsel.query.tpch

import java.io.File

import edu.uchicago.cs.encsel.dataset.parquet.ParquetWriterHelper

object LoadTPCH extends App {

  // Load TPCH Customer
  ParquetWriterHelper.write(new File("/Users/harper/TPCH/customer.tbl").toURI,
    TPCHSchema.customerSchema, new File("/Users/harper/TPCH/customer.parquet").toURI, true, "\\|")
}
