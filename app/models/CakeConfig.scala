package models

import play.api.Play
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.Play.current

import scala.io.Source

case class CakeConfig(liteUserName: String, litePassword: String) {
  def liteCredentials = (liteUserName, litePassword)
}

object CakeConfig {
  def fromFile(path: String) = Json.fromJson[CakeConfig](
    Json.parse(
      Source.fromInputStream(Play.classloader.getResourceAsStream("res/config.js")).getLines().mkString("\n")
    )
  ).get

  implicit val configRead: Reads[CakeConfig] = (
    (__ \ "lite_username").read[String] ~
    (__ \ "lite_password").read[String]
  )(CakeConfig.apply(_, _))
}