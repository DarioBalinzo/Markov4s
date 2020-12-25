package com.github.dariobalinzo.makrov4s

import com.github.dariobalinzo.markov4s._
import junit.framework.TestCase.assertEquals
import org.junit.{Rule, _}

import scala.util.Random

class Markov4sTestSuite {
  private implicit val randomGenerator: Random = new Random(0)

  @Test def `should build a valid markov chain`: Unit = {

    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, 0.3)
      .linkTo(1, 3, 0.7)
      .linkTo(2, 1, 1)
      .linkTo(3, 1, 1)

    val chain = chainBuilder.build()
    val expected = MarkovChain(
      Map(
        1 -> MarkovNode(List(OutgoingLink(0.7, 3), OutgoingLink(0.3, 2))),
        2 -> MarkovNode(List(OutgoingLink(1.0, 1))),
        3 -> MarkovNode(List(OutgoingLink(1.0, 1)))
      )
    )
    assertEquals(expected, chain)
  }

  @Test(expected = classOf[IllegalArgumentException]) def `should not build chain with wrong probabilities`: Unit = {
    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, 0.7)
      .linkTo(1, 3, 0.7)
      .linkTo(2, 1, 1)
      .linkTo(3, 1, 1)

    chainBuilder.build()
  }

  @Test(expected = classOf[IllegalArgumentException]) def `should not build node linked to non existing destination`: Unit = {
    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, 0.3)
      .linkTo(1, 3, 0.7)
      .linkTo(2, 999, 1)
      .linkTo(3, 1, 1)

    chainBuilder.build()
  }

  @Test(expected = classOf[IllegalArgumentException]) def `every node should link to at least one destination`: Unit = {
    val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, 0.3)
      .linkTo(1, 3, 0.7)

    chainBuilder.build()
  }

  @Rule def individualTestTimeout = new org.junit.rules.Timeout(1 * 1000)

  @Test def `should train a markov chain`: Unit = {
    val trainingSet = Seq("a", "b", "a", "b", "c", "d", "e", "a", "z")
    val markovChain = Markov4s.fromSequenceOfSteps(trainingSet)
    val expected = Map("e" -> MarkovNode(List(OutgoingLink(1.0, "a"))),
      "a" -> MarkovNode(List(OutgoingLink(0.6666666666666666, "b"), OutgoingLink(0.3333333333333333, "z"))),
      "b" -> MarkovNode(List(OutgoingLink(0.5, "c"), OutgoingLink(0.5, "a"))),
      "c" -> MarkovNode(List(OutgoingLink(1.0, "d"))),
      "z" -> MarkovNode(List(OutgoingLink(1.0, "z"))),
      "d" -> MarkovNode(List(OutgoingLink(1.0, "e"))))

    assertEquals(expected, markovChain.chain)
  }
}
