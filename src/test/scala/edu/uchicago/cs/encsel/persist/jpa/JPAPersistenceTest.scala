package edu.uchicago.cs.encsel.persist.jpa

import java.io.File
import java.util.ArrayList

import scala.collection.JavaConversions.asScalaBuffer

import org.junit.Before
import org.junit.Test
import org.junit.Assert._

import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table
import edu.uchicago.cs.encsel.model.DataType

class JPAPersistenceTest {

  @Before
  def cleanSchema: Unit = {
    JPAPersistence.em.getTransaction.begin

    JPAPersistence.em.createNativeQuery("DELETE FROM feature WHERE name = 'A';").executeUpdate()
    JPAPersistence.em.createNativeQuery("DELETE FROM col_data WHERE name = 'a';").executeUpdate()
    JPAPersistence.em.flush()

    JPAPersistence.em.getTransaction.commit

    JPAPersistence.em.getTransaction.begin
    var col1 = new ColumnWrapper
    col1.colName = "a"
    col1.colIndex = 5
    col1.dataType = DataType.STRING
    col1.colFile = new File("aab").toURI
    col1.origin = new File("ccd").toURI

    col1.features = new ArrayList[FeatureWrapper]

    var fea1 = new FeatureWrapper
    fea1.name = "A"
    fea1.featureType = "W"
    fea1.value = 3.5

    col1.features += fea1

    JPAPersistence.em.persist(col1)

    JPAPersistence.em.getTransaction.commit
  }

  @Test
  def testSave: Unit = {

  }

  @Test
  def testLoad: Unit = {
    var jpa = new JPAPersistence
    var cols = jpa.load()

    assertEquals(1, cols.size)
    var col = cols.iterator.next()
    assertEquals(DataType.STRING, col.dataType)
    assertEquals(5, col.colIndex)
    assertEquals("a", col.colName)
  }

  @Test
  def testClean: Unit = {

  }
}