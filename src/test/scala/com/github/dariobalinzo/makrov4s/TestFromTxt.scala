package com.github.dariobalinzo.makrov4s

import com.github.dariobalinzo.markov4s.Markov4s

import scala.util.{Random, Using}

object TestFromTxt extends App {

  implicit val randomGenerator: Random = new Random(0)

  Using(scala.io.Source.fromFile("test.txt")) { file =>
    val words = file
      .mkString
      .split("\\s+")
      .toList
      .map(_.toLowerCase)

    val markovChain = Markov4s.fromSequenceOfSteps(words)

    print(markovChain.randomWalk("non", 10).walk.mkString(" "))
  }

}
