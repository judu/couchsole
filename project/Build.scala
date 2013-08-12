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
      "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
		"Couchbase maven repository" at "http://files.couchbase.com/maven2/"
    )
  )


  lazy val main = play.Project(appName,appVersion,appDependencies).settings(baseSettings:_*)
}
