package edu.uchicago.cs.encsel.parser

import org.junit.Test
import org.junit.Assert._
import java.io.File
import edu.uchicago.cs.encsel.parser.csv.CommonsCSVParser
import edu.uchicago.cs.encsel.parser.excel.XLSXParser
import edu.uchicago.cs.encsel.parser.tsv.TSVParser
import edu.uchicago.cs.encsel.parser.json.LineJsonParser

class ParserFactoryTest {

  @Test
  def testGetParser(): Unit = {
    var csvParser = ParserFactory.getParser(new File("src/test/resource/test_guess_schema.csv").toURI())
    assertTrue(csvParser.isInstanceOf[CommonsCSVParser])
    
    var jsonParser = ParserFactory.getParser(new File("src/test/resource/test_guess_schema.json").toURI())
    assertTrue(jsonParser.isInstanceOf[LineJsonParser])
    
    var xlsxParser = ParserFactory.getParser(new File("src/test/resource/test_guess_schema.xlsx").toURI())
    assertTrue(xlsxParser.isInstanceOf[XLSXParser])
    
    var tsvParser = ParserFactory.getParser(new File("src/test/resource/test_guess_schema.tsv").toURI())
    assertTrue(tsvParser.isInstanceOf[TSVParser])
  }
}