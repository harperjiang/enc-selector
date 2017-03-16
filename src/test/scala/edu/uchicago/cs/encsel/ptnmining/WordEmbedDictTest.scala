package edu.uchicago.cs.encsel.ptnmining

import org.junit.Test
import org.junit.Assert._

class WordEmbedDictTest {

  @Test
  def testFind: Unit = {
    val words = new WordEmbedDict("/home/harper/Downloads/glove.42B.300d.txt")

    assertTrue(!words.find("st.").isEmpty)
  }

  @Test
  def testCompare: Unit = {
    val words = new WordEmbedDict("/home/harper/Downloads/glove.42B.300d.txt")

    println(words.compare("street","avenue"))
    assertTrue(0.5 < words.compare("street", "avenue"))
  }

  @Test
  def testAddPhrase: Unit = {
    val words = new WordEmbedDict("/home/harper/Downloads/glove.42B.300d.txt")

    assertTrue(words.find("good man").isEmpty)

    words.addPhrase("good man",Array("good","man"))

    assertTrue(!words.find("good man").isEmpty)

    val result = words.find("good man").get
    val good = words.find("good").get
    val man = words.find("man").get

    assertEquals(result, good.add(man))
  }
}