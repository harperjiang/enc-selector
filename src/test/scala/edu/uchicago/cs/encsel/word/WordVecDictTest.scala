package edu.uchicago.cs.encsel.word

import org.junit.Test

class WordVecDictTest {

  @Test
  def testFind: Unit = {
    val words = new WordEmbedDict("/home/harper/Downloads/glove.42B.300d.txt")

    println(words.compare("st.", "rd."))
    println(words.compare("street", "st."))
    println(words.compare("road", "rd."))
    println(words.compare("avenue", "ave."))
    println(words.compare("road", "ave."))
    println(words.compare("street", "rd."))
    println(words.compare("street", "box"))
    println(words.compare("street", "ap"))
  }
}