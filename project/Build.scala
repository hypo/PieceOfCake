import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "PieceOfCake"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      jdbc,
      "com.typesafe.slick" % "slick_2.10" % "1.0.1",
      "postgresql" % "postgresql" % "9.2-1002.jdbc4"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers ++= Seq(
        "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
        "fwbrasil.net" at "http://fwbrasil.net/maven/",
        "Typesafe Repo" at "http://repo.typesafe.com/typesafe/repo/"
      ),
      scalaVersion := "2.10.2"
    )

}
