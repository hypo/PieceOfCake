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
      cache,
      "com.typesafe.slick" %% "slick" % "2.1.0",
      "com.typesafe.play" %% "play-slick" % "0.8.0",
      "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
      "org.apache.pdfbox" % "pdfbox" % "1.8.6",
      "org.pac4j" % "play-pac4j_scala2.11" % "1.3.0-SNAPSHOT",
      "org.pac4j" % "pac4j-oauth" % "1.5.1"
    )

    val main = Project(appName, file(".")).enablePlugins(play.PlayScala).settings(
      version := appVersion,
      resolvers ++= Seq(
        "fwbrasil.net" at "http://fwbrasil.net/maven/",
        "Typesafe Repo" at "http://repo.typesafe.com/typesafe/repo/",
        "Sonatype snapshots repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
      ),
      scalaVersion := "2.11.2",
      libraryDependencies ++= appDependencies,
      scalacOptions ++= Seq("-deprecation", "-feature", "-deprecation")
    )

}
