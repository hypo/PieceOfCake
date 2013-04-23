import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "PieceOfCake"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "net.fwbrasil" %% "activate-core" % "1.2",
      "net.fwbrasil" %% "activate-prevayler" % "1.2",
      "net.fwbrasil" %% "activate-jdbc" % "1.2",
      "net.fwbrasil" %% "activate-play" % "1.2",
      "net.fwbrasil" %% "activate-mongo" % "1.2",

      "com.typesafe.slick" % "slick_2.10" % "1.0.0",
      "postgresql" % "postgresql" % "9.2-1002.jdbc4"

    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers ++= Seq(
        "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/",
        "fwbrasil.net" at "http://fwbrasil.net/maven/",
        "Typesafe Repo" at "http://repo.typesafe.com/typesafe/repo/"
      )
    )

}
