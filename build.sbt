import sbt.Keys._

name := "akka-basic-paxos"

scalaVersion := "2.12.4"

version := IO.read(file("version"))

name := "Akka-Basic-Paxos"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

val akkaVersion = "2.5.6"
val akkaHttpVersion = "10.0.10"

val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion
)

val other = Seq(
  "org.scalatest" %% "scalatest" % "3.0.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
  "com.typesafe" % "config" % "1.3.1"
)

lazy val basicPaxos = (project in file(".")).
  settings(
    name := "my-project",
    mainClass in Compile := Some("BasicPaxosSystem")
  )

libraryDependencies ++= akkaDeps ++ other

fork in run := true

fork in test := false

resourceDirectory in Compile := baseDirectory.value / "conf"

resourceDirectory in Test := baseDirectory.value / "conf"

unmanagedResourceDirectories in Compile += baseDirectory.value / "src/main/resources"

unmanagedResourceDirectories in Test += baseDirectory.value / "src/main/resources"

testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports")

mainClass in Compile := Some("BasicPaxosSystem")

enablePlugins(JavaAppPackaging)

herokuAppName in Compile := "akka-basic-paxos"

herokuFatJar in Compile := Some((assemblyOutputPath in assembly).value)

herokuProcessTypes in Compile := Map(
  "node1" -> "java -Dbasic-paxos.apiPort=8081 -Dclustering.port=2551 -jar target/scala-2.12/Akka-Basic-Paxos-assembly-0.0.1.jar",
  "node2" -> "java -Dbasic-paxos.apiPort=8082 -Dclustering.port=2552 -jar target/scala-2.12/Akka-Basic-Paxos-assembly-0.0.1.jar",
  "node3" -> "java -Dbasic-paxos.apiPort=8083 -Dclustering.port=2553 -jar target/scala-2.12/Akka-Basic-Paxos-assembly-0.0.1.jar",
  "node4" -> "java -Dbasic-paxos.apiPort=8084 -Dclustering.port=2554 -jar target/scala-2.12/Akka-Basic-Paxos-assembly-0.0.1.jar",
  "node5" -> "java -Dbasic-paxos.apiPort=8085 -Dclustering.port=2555 -jar target/scala-2.12/Akka-Basic-Paxos-assembly-0.0.1.jar",
  "node6" -> "java -Dbasic-paxos.apiPort=8086 -Dclustering.port=2556 -jar target/scala-2.12/Akka-Basic-Paxos-assembly-0.0.1.jar",
  "node7" -> "java -Dbasic-paxos.apiPort=8087 -Dclustering.port=2557 -jar target/scala-2.12/Akka-Basic-Paxos-assembly-0.0.1.jar"
)

