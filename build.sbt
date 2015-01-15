
import NativePackagerKeys._

name := "socku"

organization := "net.crispywalrus"

scalaVersion := "2.11.5"

lazy val AkkaVer = "2.3.8"
lazy val SockoVer = "0.6.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVer,
  "org.mashupbots.socko" %% "socko-webserver" % SockoVer,
  "org.mashupbots.socko" %% "socko-rest" % SockoVer,
  "org.scalatest" %% "scalatest" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer
)

packageArchetype.java_application
