package com.github.dariobalinzo.markov4s

import scala.annotation.tailrec
import scala.util.Random

case class RandomWalkResult[T](currentStep: T, walk: Seq[T])

case class MarkovChain[T](chain: Map[T, MarkovNode[T]])(implicit random: Random) {
  private val keys = chain.keySet

  val statesPointingToNonExistingState = chain.values
    .flatMap(_.links)
    .map(_.to)
    .filterNot(keys.contains)

  require(
    statesPointingToNonExistingState.isEmpty,
    s"Outgoing link pointing to a non existing node: $statesPointingToNonExistingState"
  )

  def randomWalk(startingFrom: T, numberOfSteps: Int): RandomWalkResult[T] = {
    require(numberOfSteps > 0)

    @tailrec
    def walk(remainingSteps: Int, walkAccumulator: RandomWalkResult[T]): RandomWalkResult[T] = {
      if (remainingSteps == 0) {
        walkAccumulator
      } else {
        val current = walkAccumulator.currentStep
        walk(remainingSteps - 1, RandomWalkResult(chain(current).randomStep(random), current +: walkAccumulator.walk))
      }
    }

    walk(numberOfSteps - 1, RandomWalkResult(chain(startingFrom).randomStep(random), Seq(startingFrom)))
  }

}

case class MarkovNode[T](links: Seq[OutgoingLink[T]]) {
  @tailrec
  private[markov4s] final def chooseStep(nextPercent: Double, links: Seq[OutgoingLink[T]]): T = {
    links match {
      case x :: _ if nextPercent <= x.prob => x.to
      case x :: xs => chooseStep(nextPercent - x.prob, xs)
    }
  }

  private[markov4s] def randomStep(random: Random) = {
    val nextPercent = random.nextInt(100).toDouble / 100.0
    chooseStep(nextPercent, links)
  }
}

case class OutgoingLink[T](prob: Double, to: T)

