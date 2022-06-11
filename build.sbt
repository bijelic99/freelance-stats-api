name := """freelance-stats-api"""
organization := "com.freelance-stats"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

libraryDependencies ++= Seq(
  "joda-time" % "joda-time" % "2.10.8",
  "com.typesafe.play" %% "play-json-joda" % "2.9.2",
  "org.reactivemongo" %% "play2-reactivemongo" % "1.0.10-play28",
  "org.reactivemongo" %% "reactivemongo-play-json-compat" % "1.0.10-play28",
)

scalafmtOnCompile := true

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.freelance-stats.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.freelance-stats.binders._"
