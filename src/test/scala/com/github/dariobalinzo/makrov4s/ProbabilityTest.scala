package com.github.dariobalinzo.makrov4s

import com.github.dariobalinzo.markov4s._
import junit.framework.TestCase.assertEquals
import org.junit.{Rule, _}

import scala.util.Random

class ProbabilityTest {
  private implicit val randomGenerator: Random = new Random(0)

  val p1 = new Probability(1, 6)
  val p2 = new Probability(2, 6)
  val p3 = new Probability(3, 6)

  @Test def `should sum and sort probabilities`: Unit = {
    assertEquals(1.0, (p1 + p2 + p3).toDouble)
    assertEquals(Seq(p1, p2, p3), Seq(p3, p2, p1).sorted)
  }

  @Test(expected = classOf[IllegalArgumentException]) def `should not build invalid probabilities`: Unit = {
    new Probability(2, 1)
  }

  @Test(expected = classOf[IllegalArgumentException]) def `should not build invalid probabilities (negative)`: Unit = {
    new Probability(-2, 1)
  }

  @Rule def individualTestTimeout = new org.junit.rules.Timeout(1 * 1000)
}
