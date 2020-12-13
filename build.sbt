name := "markov4s"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
testOptions in Test += Tests.Argument(TestFrameworks.JUnit, "-a", "-v", "-s")
