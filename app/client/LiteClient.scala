package client

import play.api.libs.json.{Reads, Json}
import client.LiteObjects._
import play.api.libs.ws.{WSAuthScheme, WS}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

class LiteClient(
  credential: (String, String),
  baseURL: String
) {

  import LiteClient._

  def login(email: String, password: String) =
    service("/api/users/check_login", accessToken).post(Map(
      "email" -> Seq(email),
      "password" -> Seq(password)
    )) map { resp => resp.json.asOpt[LoginResponse] }

  def signup(user: LiteUser) =
    service("/api/users", accessToken).post(Json.toJson(user)) map { resp => resp.json.asOpt[LoginResponse] }

  def createOrder(order: LiteOrder) =
    service("/api/orders/pieces", accessToken).post(Json.toJson(order)) map { resp => resp.json.asOpt[OrderResponse] }

  def atmAccount(orderId: Int) =
    service(s"/api/orders/$orderId/atm", accessToken).get map { resp => resp.json.asOpt[ATMAccountResponse] }

  def creditCard(orderId: Int, creditCard: CreditCard) =
    service(s"/api/orders/$orderId/credit_card", accessToken).post(Map(
      "number" -> Seq(creditCard.number),
      "month" -> Seq(creditCard.month),
      "year" -> Seq(creditCard.year),
      "cvv2" -> Seq(creditCard.cvv)
    )) map { resp => resp.json.asOpt[CreditCardResponse] }

  def estimatedShippingDate() = service("/api/orders/estimated_shipping_date", None).get map { resp => resp.json.asOpt[EstimatedShippingDateResponse] }

  private lazy val accessToken : Option[AccessToken] = Await.result(
    service("/oauth2/access_token", None)
      .withAuth(credential._1, credential._2, WSAuthScheme.BASIC)
      .post(Map(
        "grant_type" -> Seq("client_credentials")
      )) map { resp => resp.json.asOpt[AccessToken] }
  , Duration.Inf)

  private[this] def p(path: String) = s"$baseURL$path"

  private[this] def service(path: String, token: Option[AccessToken]) = {
    val svc = WS.url(p(path))
    token map { t =>
      svc.withHeaders(("Authorization", t.toString))
    } getOrElse svc
  }

}

object LiteClient {

  def apply(credentials: (String, String), baseURL: String) = new LiteClient(credentials, baseURL)

  case class CreditCard(number: String, month: String, year: String, cvv: String)
  def mkCard(number: String, expiry: String, cvv: String) = {
    if (expiry.length != 4) {
      None
    } else {

      val (mm, yy) = (expiry.splitAt(2))
      if (mm.toInt < 0 || mm.toInt > 12 || yy.toInt < 0)
        None
      else
        Some(CreditCard(number, mm, yy, cvv))
    }
  }

}