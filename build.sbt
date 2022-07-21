name := """json-validation-service"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  evolutions,
  "org.xerial" % "sqlite-jdbc" % "3.36.0.3",
  "com.github.java-json-tools" % "json-schema-validator" % "2.2.14"
)
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "org.mockito" % "mockito-core" % "4.6.1" % Test,
  "com.h2database" % "h2" % "1.4.192" % Test
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
