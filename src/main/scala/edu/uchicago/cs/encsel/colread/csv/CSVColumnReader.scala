package edu.uchicago.cs.encsel.colread.csv

import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.net.URI

import edu.uchicago.cs.encsel.colread.ColumnReader
import edu.uchicago.cs.encsel.colread.Schema
import edu.uchicago.cs.encsel.model.Column
import edu.uchicago.cs.encsel.colread.ParserColumnReader

class CSVColumnReader extends ParserColumnReader(new CSVParser)