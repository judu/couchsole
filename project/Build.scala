import sbt._
import Keys._

object ApplicationBuild extends Build {

  val appName         = "couchsole"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "couchbase" % "couchbase-client" % "1.1.5",
    "org.scalaz" %% "scalaz-core" % "7.0.2"
  )

  val baseSettings = Seq(
    scalaVersion := "2.10.2",
    scalacOptions ++= Seq("-feature", "-Xlint", "-deprecation", "-unchecked"),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
    )
  )


  lazy val main = play.Project(appName,appVersion,appDependencies).settings(baseSettings:_*) dependsOn macroTemplates

  lazy val macroTemplates = Project("macro-templates", file("macro-templates")).settings(baseSettings:_*).settings(
    scalaVersion := "2.10.3-SNAPSHOT",
    scalaOrganization := "org.scala-lang.macro-paradise",
    libraryDependencies <+= (scalaOrganization, scalaVersion)(_ % "scala-reflect" % _),
    libraryDependencies += "play" %% "play" % "2.1.2"
  )
}
