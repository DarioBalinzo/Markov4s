package com.github.dariobalinzo.markov4s

import scala.annotation.tailrec
import scala.util.Random

private case class Link[T](from: T, to: T)


private case class To[T](to: T, times: Int) {
  private[markov4s] def withProb(prob: Double) = ToWithProb(to, prob)
}

private case class ToWithProb[T](to: T, prob: Double)

class Training[T] {

  @tailrec
  private def parseLink(accumulator: Seq[Link[T]], prevState: T, trainingSet: Seq[T]): Seq[Link[T]] = {
    trainingSet match {
      case Nil => accumulator
      case nextState :: xs => parseLink(Link(prevState, nextState) +: accumulator, nextState, xs)
    }
  }

  private def countTotalNextStateOccurrences(occurs: Seq[Link[T]]) = occurs.size

  private def countTo(occurs: Seq[Link[T]]): Seq[To[T]] = {
    val grouped = occurs.groupBy(_.to)
    val groupedCount = grouped.map(item => (item._1, item._2.size))
    groupedCount.map(item => To(item._1, item._2)).toSeq
  }


  private def countToProb(total: Map[T, Int], from: T, occurs: Seq[To[T]]): Seq[ToWithProb[T]] = {
    if (occurs.isEmpty) {
      Seq(ToWithProb(from, 1.0))
    } else {
      occurs.map { x =>
        val totalForX = total(from)
        val prob = x.times.toDouble / totalForX.toDouble
        x.withProb(prob)
      }
    }
  }

  def train(trainingSet: Seq[T])(implicit random: Random): MarkovChain[T] = {
    val occurrences = trainingSet match {
      case Nil | _ :: Nil | _ :: _ :: Nil => throw new IllegalArgumentException("input too small")
      case first :: others => parseLink(Seq.empty, first, others)
    }

    val states = occurrences.groupBy(link => link.from)
    val statesWithTotalOutgoing = states.map(item => (item._1, countTotalNextStateOccurrences(item._2)))
    val statesTo = states.map(item => (item._1, countTo(item._2)))
    val statesWithProb = statesTo.map(item => (item._1, countToProb(statesWithTotalOutgoing, item._1, item._2)))

    val markovBuilder = statesWithProb.foldRight(ChainBuilder[T]())((item, chainBuilder) => {
      val from = item._1
      val destinations = item._2

      destinations.foldRight(chainBuilder)(
        (to, builder) => builder.linkTo(from, to.to, to.prob)
      )
    })

    markovBuilder.build()
  }

}
