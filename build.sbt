
import NativePackagerKeys._

name := "socku"

organization := "net.crispywalrus"

scalaVersion := "2.11.5"

scalacOptions++= Seq(
  "-feature",
  "-deprecation",
  "-language:postfixOps"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

lazy val AkkaVer = "2.3.9"
lazy val SockoVer = "0.6.0"
lazy val JacksonVer = "2.5.0"
lazy val MacwireVer = "0.8.0"
lazy val AkkaStreamz = "1.0-M2"

resolvers++= Seq(
  "bintray/non" at "http://dl.bintray.com/non/maven"
)

libraryDependencies++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVer,
  "com.typesafe.akka" %% "akka-stream-experimental" % AkkaStreamz,
  "com.typesafe.akka" % "akka-http-experimental_2.11" % AkkaStreamz,
  "org.mashupbots.socko" %% "socko-webserver" % SockoVer,
  "org.mashupbots.socko" %% "socko-rest" % SockoVer,
  "org.mashupbots.socko" %% "socko-buildtools" % SockoVer,
  "org.typelevel" %% "machinist" % "0.3.0",
  "com.github.mpilquist" %% "simulacrum" % "0.2.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.2",
  "com.softwaremill.macwire" %% "macros" % MacwireVer,
  "com.softwaremill.macwire" %% "runtime" % MacwireVer,
  "com.jteigen" %% "linx" % "0.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVer,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVer,
  "com.fasterxml.jackson.module" % "jackson-module-afterburner" % JacksonVer,
  "io.orchestrate" % "orchestrate-client" % "0.7.0",
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer,
  "org.scalatest" %% "scalatest" % "2.2.3" % Test,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer % Test
)

packageArchetype.java_application
