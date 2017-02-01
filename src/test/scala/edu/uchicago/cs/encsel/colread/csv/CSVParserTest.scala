package edu.uchicago.cs.encsel.colread.csv

import org.junit.Assert.assertEquals
import org.junit.Test

class CSVParserTest {

  @Test
  def testParseLine(): Unit = {

    var parser = new CSVParser()

    var input = "a,b,c,d,e";
    var output = parser.parseLine(input)

    assertEquals(5, output.length)

    input = "a,3,7,\"323,m4,2,34\""
    output = parser.parseLine(input)

    assertEquals(4, output.length)

    input = """a,b,"5,23.24",53.4149,132"""
    output = parser.parseLine(input)

    assertEquals(5, output.length)

    input = """,,,,,"""
    output = parser.parseLine(input)

    assertEquals(6, output.length)
  }

  @Test
  def testSpecialLine(): Unit = {
    var parser = new CSVParser()
    var line1 = """19823,HT223608,03/29/2011 08:11:00 AM,009XX W FULLERTON AVE,0110,HOMICIDE,FIRST DEGREE MURDER,"CTA ""L"" PLATFORM",true,false,1933,019,43,7,01A,1169577,1916141,2011,08/17/2015 03:03:40 PM,41.925398449,-87.652311296,"(41.925398449, -87.652311296)""""
    var line2 = """| 34. FLOORS: CONSTRUCTED PER CODE, CLEANED, GOOD REPAIR, COVING INSTALLED, DUST-LESS CLEANING METHODS USED - Comments: VIOLATION NOW CORRECTED AND ABATED, | 37. TOILET ROOM DOORS SELF CLOSING: DRESSING ROOMS WITH LOCKERS PROVIDED: COMPLETE SEPARATION FROM LIVING/SLEEPING QUARTERS - Comments: VIOLATION NOW CORRECTED AND ABATED, | 38. VENTILATION: ROOMS AND EQUIPMENT VENTED AS REQUIRED: PLUMBING: INSTALLED AND MAINTAINED - Comments: VIOLATION NOW CORRECTED AND ABATED, | 40. REFRIGERATION AND METAL STEM THERMOMETERS PROVIDED AND CONSPICUOUS - Comments: VIOLATION NOW CORRECTED AND ABATED, | 41. PREMISES MAINTAINED FREE OF LITTER, UNNECESSARY ARTICLES, CLEANING  EQUIPMENT PROPERLY STORED - Comments: CLUTTER 2ND AND FIRST FLOOR STORAGES, NOT CLEAN, INSTRUCTED TO BETTER MAINTAIN, | 43. FOOD (ICE) DISPENSING UTENSILS, WASH CLOTHS PROPERLY STORED - Comments: VIOLATION NOW CORRECTED AND ABATED,",41.9396542641,-87.7663905154,"(41.9396542640577, -87.76639051535662)""""

    var output = parser.parseLine(line1)
    assertEquals(22, output.length)
    parser.parseLine(line2)
  }
}