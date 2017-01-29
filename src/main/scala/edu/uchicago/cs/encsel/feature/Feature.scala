package edu.uchicago.cs.encsel.feature

trait Feature extends Serializable {
  def name: String
  def value: Double
}