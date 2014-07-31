package client

import dispatch._
import Defaults._
import play.api.libs.json.{Reads, Json}
import client.LiteObjects._

class LiteClient {

  import LiteClient._

  def login(email: String, password: String) = getAccessToken flatMap { accessToken =>
    val svc = servicePost("/api/users/check_login", accessToken) << Map(
      "email" -> email,
      "password" -> password
    )
    Http(svc OK as.String) map { parseJSON[LoginResponse](_) }
  }

  private def getAccessToken() = {
    val svc = servicePost("/oauth2/access_token", None) << Map(
      "grant_type" -> "client_credentials"
    ) <:< Map("Authorization" -> s"Basic $credential")

    Http(svc OK as.String) map { parseJSON[AccessToken](_) }
  }

}

object LiteClient {

  val credential = "WW1aak5ERmpZakl0TWpCbE1DMDBZVGcxTFRnelpUSXROekEyWXpnM1pqSTRNekF6Ok1ERm1OakpoT0dRdE5UQTNOUzAwWWpjNUxXSXdPR1V0T0dSbVlqVTVaVFJoTm1JeA=="
  def p(path: String) = s"http://lite.hypo.cc$path"
  def service(path: String, token: Option[AccessToken]) = {
    val svc = url(p(path))
    token map { t =>
      svc <:< Map("Authorization" -> t.toString)
    } getOrElse svc
  }
  def servicePost(path: String, token: Option[AccessToken]) = {
    val svc = service(path, token)
    svc.setMethod("POST")
  }

  def parseJSON[T](resp: String)(implicit read: Reads[T]) = Json.fromJson[T](Json.parse(resp)).fold(
    error => None,
    (thing: T) => Some(thing)
  )

}