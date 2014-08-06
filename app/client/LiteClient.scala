package client

import play.api.libs.json.{Reads, Json}
import client.LiteObjects._
import play.api.libs.ws.{WSAuthScheme, WS}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

class LiteClient {

  import LiteClient._

  def login(email: String, password: String) = getAccessToken flatMap { accessToken =>
    service("/api/users/check_login", accessToken).post(Map(
      "email" -> Seq(email),
      "password" -> Seq(password)
    )) map { resp => resp.json.asOpt[LoginResponse] }
  }

  def signup(user: LiteUser) = getAccessToken flatMap { accessToken =>
    service("/api/users", accessToken).post(Json.toJson(user)) map { resp => resp.json.asOpt[LoginResponse] }
  }

  def createOrder(order: LiteOrder) = getAccessToken flatMap { accessToken =>
    service("/api/orders/pieces", accessToken).post(Json.toJson(order)) map { resp => resp.json.asOpt[OrderResponse] }
  }

  def atmAccount(orderId: Int) = getAccessToken flatMap { accessToken =>
    service(s"/api/orders/$orderId/atm", accessToken).get map { resp => resp.json.asOpt[ATMAccountResponse] }
  }

  private def getAccessToken() : Future[Option[AccessToken]] = {
    service("/oauth2/access_token", None)
      .withAuth(credential._1, credential._2, WSAuthScheme.BASIC)
      .post(Map(
        "grant_type" -> Seq("client_credentials")
      )) map { resp => resp.json.asOpt[AccessToken] }
  }

}

object LiteClient {

  val credential = ("YmZjNDFjYjItMjBlMC00YTg1LTgzZTItNzA2Yzg3ZjI4MzAz", "MDFmNjJhOGQtNTA3NS00Yjc5LWIwOGUtOGRmYjU5ZTRhNmIx")
  def p(path: String) = s"http://lite.hypo.cc$path"
  def service(path: String, token: Option[AccessToken]) = {
    val svc = WS.url(p(path))
    token map { t =>
      svc.withHeaders(("Authorization", t.toString))
    } getOrElse svc
  }

}