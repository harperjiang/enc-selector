package edu.uchicago.cs.encsel.dataset.persist.jpa

import java.io.File
import java.util.ArrayList

import scala.collection.JavaConversions._

import org.junit.Assert._
import org.junit.Before
import org.junit.Test

import edu.uchicago.cs.encsel.dataset.column.Column
import edu.uchicago.cs.encsel.dataset.feature.Feature
import edu.uchicago.cs.encsel.model.DataType
import edu.uchicago.cs.encsel.dataset.persist.jpa.ColumnWrapper;
import edu.uchicago.cs.encsel.dataset.persist.jpa.JPAPersistence;

import javax.persistence.Embeddable
import javax.persistence.Entity
import javax.persistence.Table

class JPAPersistenceTest {

  @Before
  def cleanSchema: Unit = {
    var em = JPAPersistence.emf.createEntityManager()
    em.getTransaction.begin

    em.createNativeQuery("DELETE FROM feature WHERE 1 = 1;").executeUpdate()
    em.createNativeQuery("DELETE FROM col_data WHERE 1 = 1;").executeUpdate()
    em.flush()

    em.getTransaction.commit

    em.getTransaction.begin
    var col1 = new ColumnWrapper
    col1.id = 2
    col1.colName = "a"
    col1.colIndex = 5
    col1.dataType = DataType.STRING
    col1.colFile = new File("aab").toURI
    col1.origin = new File("ccd").toURI

    col1.features = new ArrayList[Feature]

    var fea1 = new Feature
    fea1.name = "M"
    fea1.featureType = "P"
    fea1.value = 2.4

    col1.features += fea1

    em.persist(col1)

    em.getTransaction.commit
    em.close
  }

  @Test
  def testSaveNew: Unit = {
    var jpa = new JPAPersistence

    var col1 = new Column(new File("dd").toURI, 3, "m", DataType.INTEGER)
    col1.colFile = new File("tt").toURI

    col1.features = new ArrayList[Feature]

    var fea1 = new Feature("W", "A", 3.5)

    col1.features = Array(fea1).toList

    jpa.save(Array(col1))

    var cols = jpa.load()

    assertEquals(2, cols.size)

    cols.foreach(col => {
      col.colIndex match {
        case 3 => {
          assertEquals(DataType.INTEGER, col.dataType)
          assertEquals("m", col.colName)
          var feature = col.features.iterator.next
          assertEquals("W", feature.featureType)
          assertEquals("A", feature.name)
          assertEquals(3.5, feature.value, 0.01)
        }
        case 5 => {
          assertEquals(DataType.STRING, col.dataType)
          assertEquals("a", col.colName)
          var feature = col.features.iterator.next
          assertEquals("P", feature.featureType)
          assertEquals("M", feature.name)
          assertEquals(2.4, feature.value, 0.01)
        }
      }
    })
  }

  @Test
  def testSaveMerge: Unit = {
    var jpa = new JPAPersistence
    var cols = jpa.load().toArray
    assertEquals(1, cols.size)

    cols(0).features += new Feature("T", "PP", 3.25)
    jpa.save(cols)

    var newcols = jpa.load().toArray

    assertEquals(1, cols.size)
    var features = cols(0).features
    assertEquals(2, features.size)
    assertEquals("M", features(0).name)
    assertEquals("PP", features(1).name)
  }

  @Test
  def testUpdate: Unit = {
    var jpa = new JPAPersistence

    var col1 = new ColumnWrapper()
    col1.origin = new File("dd").toURI
    col1.colIndex = 3
    col1.colName = "m"
    col1.dataType = DataType.INTEGER
    col1.id = 2
    col1.colFile = new File("tt").toURI

    col1.features = new ArrayList[Feature]

    var fea1 = new Feature("W", "A", 3.5)

    col1.features = Array[Feature](fea1).toList

    jpa.save(Array[Column](col1))

    var cols = jpa.load().toArray

    assertEquals(1, cols.size)
    var col = cols(0)
    assertEquals(1, col.features.size())
    var f = col.features.iterator().next()
  }

  @Test
  def testLoad: Unit = {
    var jpa = new JPAPersistence
    var cols = jpa.load().toArray

    assertEquals(1, cols.size)
    var col = cols(0)
    assertEquals(DataType.STRING, col.dataType)
    assertEquals(5, col.colIndex)
    assertEquals("a", col.colName)
  }

  @Test
  def testClean: Unit = {

  }
}