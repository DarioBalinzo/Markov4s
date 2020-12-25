# Markov4s
Simple Markov chains implementation using Scala.
The markov chain is designed as an immutable data structure.

You can train a markov chain from a sequence of steps. For example here we are training a Markov chain
starting from a txt file and then generating random text with a random walk: 
```scala
Using(scala.io.Source.fromFile("test.txt")) { file =>
    val sequenceOfWords = file
      .mkString
      .split("\\s+")
      .toList
      .map(_.toLowerCase)
  
    val markovChain = Markov4s.fromSequenceOfSteps(words)

    val numberOfSteps = 10
    val randomPhrase = markovChain.randomWalk("scala", numberOfSteps)
      .walk
      .mkString(" ")
  }
```

Otherwise you can build the Markov chain using the builder:
```scala
val chainBuilder = ChainBuilder[Int]()
      .linkTo(1, 2, new Probability(3, 10))
      .linkTo(1, 3, new Probability(7, 10))
      .linkTo(2, 1, Probability.one)
      .linkTo(3, 1, Probability.one)
```
