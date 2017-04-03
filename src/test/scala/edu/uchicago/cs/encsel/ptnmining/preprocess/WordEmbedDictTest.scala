package edu.uchicago.cs.encsel.ptnmining.preprocess

import org.junit.Assert._
import org.junit.Test

class WordEmbedDictTest {

  @Test
  def testOutput:Unit = {
    val words = new WordEmbedDict("/home/harper/Downloads/glove.42B.300d.txt")
    println(words.find("rhode"))
    println(words.find("island"))
  }

  @Test
  def testFind: Unit = {
    val words = new WordEmbedDict("/home/harper/Downloads/glove.42B.300d.txt")

    assertTrue(words.find("st.").isDefined)
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

    assertTrue(words.find("good man").isDefined)

    val result = words.find("good man").get
    val good = words.find("good").get
    val man = words.find("man").get

    assertEquals(result, good.add(man))
  }
}