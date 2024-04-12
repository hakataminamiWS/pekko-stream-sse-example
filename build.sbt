name := """pekko-stream-sse-example"""
organization := "com.web-noren"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.13"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test

val PekkoVersion = "1.0.2"
libraryDependencies += "org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion
libraryDependencies += "org.apache.pekko" %% "pekko-stream-typed" % PekkoVersion
libraryDependencies += "org.apache.pekko" %% "pekko-cluster-typed" % PekkoVersion

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.web-noren.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.web-noren.binders._"
