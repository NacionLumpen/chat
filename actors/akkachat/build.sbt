name := "akkachat"

organization := "com.nacionlumpen"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "spray repo" at "http://nightlies.spray.io"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.12",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "io.spray" %% "spray-util" % "1.3.4",
  "io.spray" %% "spray-client" % "1.3.4",
  "io.spray" %% "spray-testkit" % "1.3.4",
  "org.scalaz" %% "scalaz-core" % "7.2.7",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4",
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test"
)
