name := "akka-basic-paxos"

scalaVersion := "2.12.4"

version := IO.read(file("version"))

name := "Akka Basic Paxos"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val akkaVersion = "2.5.6"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe" % "config" % "1.3.1",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion
)

