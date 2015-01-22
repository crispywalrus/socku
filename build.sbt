
import NativePackagerKeys._

name := "socku"

organization := "net.crispywalrus"

scalaVersion := "2.11.5"

lazy val AkkaVer = "2.3.9"
lazy val SockoVer = "0.6.0"
lazy val JacksonVer = "2.5.0"
lazy val MacwireVer = "0.8.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVer,
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-M2",
  "org.mashupbots.socko" %% "socko-webserver" % SockoVer,
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVer,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVer,
  "com.fasterxml.jackson.module" % "jackson-module-afterburner" % JacksonVer,
  "com.softwaremill.macwire" %% "macros" % MacwireVer,
  "com.softwaremill.macwire" %% "runtime" % MacwireVer,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer,
  "org.scalatest" %% "scalatest" % "2.2.3" % Test,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer % Test
)

packageArchetype.java_application
