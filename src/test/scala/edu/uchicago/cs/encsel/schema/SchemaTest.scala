package edu.uchicago.cs.encsel.schema

import edu.uchicago.cs.encsel.app.DataCollectorForTest
import org.junit.Test
import org.junit.Assert._
import java.io.File

class SchemaTest {
  @Test
  def testGetSchema(): Unit = {
    var schema = Schema.getSchema(new File("src/test/resource/find_schema.csv").toURI())
    assertEquals(3, schema.columns.length)

    schema = Schema.getSchema(new File("src/test/resource/find_schema2.tsv").toURI())
    assertEquals(6, schema.columns.length)

    schema = Schema.getSchema(new File("src/test/resource/fuzzy_find_schema_3.csv").toURI())
    assertEquals(5, schema.columns.length)
  }
}