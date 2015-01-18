
import NativePackagerKeys._

name := "socku"

organization := "net.crispywalrus"

scalaVersion := "2.11.5"

lazy val AkkaVer = "2.3.8"
lazy val SockoVer = "0.6.0"
lazy val JacksonVer = "2.4.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVer,
  "org.mashupbots.socko" %% "socko-webserver" % SockoVer,
  "org.uncommons.maths" % "uncommons-maths" % "1.2.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVer,
  "com.fasterxml.jackson.module" % "jackson-module-afterburner" % JacksonVer,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVer,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk7" % JacksonVer,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-guava" % JacksonVer,
  "com.google.guava" % "guava" % "15.0",
  "org.scalatest" %% "scalatest" % "2.2.3" % Test,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer % Test
)

packageArchetype.java_application
