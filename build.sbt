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
lazy val JacksonVer = "2.5.1"
lazy val MacwireVer = "0.8.0"
lazy val AkkaStreamz = "1.0-M3"

resolvers++= Seq(
  "bintray/non" at "http://dl.bintray.com/non/maven",
  "brando" at "http://chrisdinn.github.io/releases/"
)

libraryDependencies++= Seq(
  "com.typesafe.akka" %% "akka-actor" % AkkaVer,
  "com.typesafe.akka" %% "akka-stream-experimental" % AkkaStreamz,
  "com.typesafe.akka" % "akka-http-experimental_2.11" % AkkaStreamz,
  "org.mashupbots.socko" %% "socko-webserver" % SockoVer,
  "org.mashupbots.socko" %% "socko-rest" % SockoVer,
  "org.mashupbots.socko" %% "socko-buildtools" % SockoVer,
  "com.digital-achiever" %% "brando" % "2.0.6",
  "com.netaporter" %% "scala-uri" % "0.4.4",
  "org.typelevel" %% "machinist" % "0.3.1",
  "com.github.mpilquist" %% "simulacrum" % "0.2.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.3",
  "com.softwaremill.macwire" %% "macros" % MacwireVer,
  "com.softwaremill.macwire" %% "runtime" % MacwireVer,
  "com.jteigen" %% "linx" % "0.2",
  "me.lessis" %% "zoey-core" % "0.1.2",
  "com.fasterxml.jackson.core" % "jackson-databind" % JacksonVer,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVer,
  "com.fasterxml.jackson.module" % "jackson-module-afterburner" % JacksonVer,
  "io.orchestrate" % "orchestrate-client" % "0.8.0",
  "com.spotify" % "dns" % "2.2.0",
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer,
  "org.scalatest" %% "scalatest" % "2.2.4" % Test,
  "com.typesafe.akka" %% "akka-testkit" % AkkaVer % Test
)

enablePlugins(JavaAppPackaging,DockerPlugin)
