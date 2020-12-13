package com.github.dariobalinzo.markov4s

import scala.annotation.tailrec
import scala.util.Random

case class RandomWalkResult[T](currentStep: T, walk: Seq[T])

case class MarkovChain[T](chain: Map[T, MarkovNode[T]])(implicit random: Random) {
  private val keys = chain.keySet
  require(
    chain.values
      .flatMap(_.links)
      .map(_.to)
      .forall(keys.contains),
    "Outgoing link pointing to a non existing node"
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

case class ChainBuilder[T](chain: Map[T, NodeBuilder[T]] = Map.empty[T, NodeBuilder[T]])(implicit random: Random) {

  def linkTo(from: T, to: T, prob: Double): ChainBuilder[T] = {
    val chainWithFrom = if (!chain.contains(from)) {
      chain + (from -> NodeBuilder(from))
    } else {
      chain
    }

    val chainWithTo = if (!chainWithFrom.contains(to)) {
      chainWithFrom + (to -> NodeBuilder(to))
    } else {
      chainWithFrom
    }

    val fromBuilder = chainWithTo(from)
    val newFromBuilder = fromBuilder.linkedTo(to, prob)
    copy((chain - from) + (from -> newFromBuilder))
  }

  def build(): MarkovChain[T] = {
    MarkovChain(chain.view.mapValues(_.toNode()).toMap)
  }
}

case class NodeBuilder[T](value: T, links: Seq[OutgoingLink[T]] = List.empty) {
  private[markov4s] def linkedTo(to: T, prob: Double) = copy(links = OutgoingLink(prob, to) +: this.links)

  private[markov4s] def toNode(): MarkovNode[T] = {
    require(links.map(_.prob).sum == 1.0, "The sum of all the outgoing probabilities should be 1.0")
    MarkovNode(links.sortBy(_.prob).reverse)
  }
}

case class OutgoingLink[T](prob: Double, to: T)

