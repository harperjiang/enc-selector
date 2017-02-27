package edu.uchicago.cs.encsel.ndnn.example.lstm

import edu.uchicago.cs.encsel.ndnn.rnn.LSTMDataset

object LSTM extends App {

  val datafile = "/home/harper/dataset/lstm/ptb.train.txt"

  val lstmds = new LSTMDataset(datafile)

  val trainer = new LSTMTrainer(lstmds, 1, 500)
  
  trainer.train(30, true)
}