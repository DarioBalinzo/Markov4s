package com.github.dariobalinzo.makrov4s

import com.github.dariobalinzo.markov4s.Training

import scala.util.Random

object TestMain extends App {

  implicit val randomGenerator: Random = new Random(0)

  val file = scala.io.Source.fromFile("/home/dario/test.txt")
  val words = file.mkString.split("\\s+").toList.map(_.toLowerCase)

  val training = new Training[String]()
  val markovChain = training.train(words)

  print(markovChain.randomWalk("non", 10).walk.reverse.mkString(" "))

  file.close()

}
