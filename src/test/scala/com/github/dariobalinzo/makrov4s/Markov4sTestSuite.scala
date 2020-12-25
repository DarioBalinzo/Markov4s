package com.github.dariobalinzo.makrov4s

import com.github.dariobalinzo.markov4s._
import junit.framework.TestCase.assertEquals
import org.junit.{Rule, _}

import scala.util.Random

class Markov4sTestSuite {
  private implicit val randomGenerator: Random = new Random(0)

  @Test def `should train a markov chain from a training set`: Unit = {
    val trainingSet = Seq("a", "b", "a", "b", "c", "d", "e", "a", "z")
    val markovChain = Markov4s.fromSequenceOfSteps(trainingSet)
    val expected = Map("e" -> MarkovNode(List(OutgoingLink(Probability.one, "a"))),
      "a" -> MarkovNode(List(OutgoingLink(new Probability(2, 3), "b"), OutgoingLink(new Probability(1, 3), "z"))),
      "b" -> MarkovNode(List(OutgoingLink(new Probability(1, 2), "c"), OutgoingLink(new Probability(1, 2), "a"))),
      "c" -> MarkovNode(List(OutgoingLink(Probability.one, "d"))),
      "z" -> MarkovNode(List(OutgoingLink(Probability.one, "z"))),
      "d" -> MarkovNode(List(OutgoingLink(Probability.one, "e"))))

    assertEquals(expected, markovChain.chain)
  }

  @Test def `should train a markov chain with last step not unique`: Unit = {
    val trainingSet = Seq("a", "b", "a", "b", "c", "d", "e", "a")
    val markovChain = Markov4s.fromSequenceOfSteps(trainingSet)
    val expected = Map("e" -> MarkovNode(List(OutgoingLink(Probability.one, "a"))),
      "a" -> MarkovNode(List(OutgoingLink(Probability.one, "b"))),
      "b" -> MarkovNode(List(OutgoingLink(new Probability(1, 2), "c"), OutgoingLink(new Probability(1, 2), "a"))),
      "c" -> MarkovNode(List(OutgoingLink(Probability.one, "d"))),
      "d" -> MarkovNode(List(OutgoingLink(Probability.one, "e"))))

    assertEquals(expected, markovChain.chain)
  }

  @Test def `should build a valid markov chain`: Unit = {
    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, new Probability(3, 10))
      .linkTo(1, 3, new Probability(7, 10))
      .linkTo(2, 1, Probability.one)
      .linkTo(3, 1, Probability.one)

    val chain = chainBuilder.build()
    val expected = MarkovChain(
      Map(
        1 -> MarkovNode(List(OutgoingLink(new Probability(7, 10), 3), OutgoingLink(new Probability(3, 10), 2))),
        2 -> MarkovNode(List(OutgoingLink(Probability.one, 1))),
        3 -> MarkovNode(List(OutgoingLink(Probability.one, 1)))
      )
    )
    assertEquals(expected, chain)
  }

  @Test def `should generate a random walk`: Unit = {

    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, new Probability(3, 10))
      .linkTo(1, 3, new Probability(7, 10))
      .linkTo(2, 1, Probability.one)
      .linkTo(3, 1, Probability.one)

    val chain = chainBuilder.build()
    val result = chain.randomWalk(1, 10)

    val expected = Seq(1, 3, 1, 3, 1, 3, 1, 2, 1, 3)
    assertEquals(expected, result.walk)
  }

  @Test(expected = classOf[IllegalArgumentException]) def `should not build chain with wrong probabilities`: Unit = {
    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, new Probability(7, 10))
      .linkTo(1, 3, new Probability(7, 10))
      .linkTo(2, 1, Probability.one)
      .linkTo(3, 1, Probability.one)

    chainBuilder.build()
  }

  @Test(expected = classOf[IllegalArgumentException]) def `should not build node linked to non existing destination`: Unit = {
    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, new Probability(3, 10))
      .linkTo(1, 3, new Probability(7, 10))
      .linkTo(2, 999, Probability.one)
      .linkTo(3, 1, Probability.one)

    chainBuilder.build()
  }

  @Test(expected = classOf[IllegalArgumentException]) def `every node should link to at least one destination`: Unit = {
    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, new Probability(3, 10))
      .linkTo(1, 3, new Probability(7, 10))

    chainBuilder.build()
  }

  @Rule def individualTestTimeout = new org.junit.rules.Timeout(1 * 1000)
}
