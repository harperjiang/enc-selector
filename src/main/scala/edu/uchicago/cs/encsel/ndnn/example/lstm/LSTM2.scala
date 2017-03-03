package edu.uchicago.cs.encsel.ndnn.example.lstm

import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset
import org.nd4j.linalg.factory.Nd4j
import java.util.Random
import scala.collection.JavaConversions._

object LSTM2 extends App {
  //  val datafile = "C:/Users/Cathy/dataset/lstm/ptb.train.txt"
  //  val lstmds = new LSTMDataset(datafile)
  //  
  val charSize = 100
  val dim = 200
  val embed = (0 until charSize).map(i => Nd4j.createUninitialized(dim).assign(i)).toArray
  val size = 50
  val random = new Random(System.currentTimeMillis())

  for (i <- 0 to 10000) {
    val array = (0 until size).map(i => Math.abs(random.nextInt()) % charSize).map { embed(_) }.toList
    val matrix = Nd4j.create(array, Array(size, dim))
  }

}