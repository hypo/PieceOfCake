import sbt._
import Keys._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

    val appName         = "PieceOfCake"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      jdbc,
      ws,
      "com.typesafe.slick" %% "slick" % "2.1.0",
      "com.typesafe.play" %% "play-slick" % "0.8.0",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41"
    )

    val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
      version := appVersion,
      resolvers ++= Seq(
        "fwbrasil.net" at "http://fwbrasil.net/maven/",
        "Typesafe Repo" at "http://repo.typesafe.com/typesafe/repo/"
      ),
      scalaVersion := "2.11.2",
      libraryDependencies ++= appDependencies,
      scalacOptions ++= Seq("-deprecation", "-feature", "-deprecation")
    )

}
