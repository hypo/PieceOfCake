package controllers

import client.LiteClient
import client.LiteObjects.{LiteUser, LoginResponse}
import models.PiecesPricingStrategy
import models.PricingStrategyWriter._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object ApiController extends Controller {

  def login() = Action.async { request =>
    val credentials = for (
      params <- request.body.asFormUrlEncoded;
      emails <- params.get("email");
      passwords <- params.get("password")
    ) yield (emails.head, passwords.head)

    credentials map { case (email, password) =>
      new LiteClient().login(email, password) map {
        case Some(LoginResponse("ok", "verified", user)) => Ok(Json.stringify(loginOkResponse(user.get)))
        case _ => Ok(Json.stringify(loginFailResponse))
      }
    } getOrElse Future { BadRequest(Json.stringify(loginFailResponse)) }
  }

  def pricingStrategy() = Action { request =>
    val jsonObj = Json.toJson(PiecesPricingStrategy)

    request.getQueryString("callback") match {
      case Some(cb) => Ok(s"${cb}(${jsonObj})")
      case None     => Ok(jsonObj)
    }
  }

  def loginOkResponse(user: LiteUser) = Json.obj(
    "actions" -> Json.arr("push-data", "push-card"),
    "card"    -> "card_shipping",
    "changes" -> Json.arr(
      Json.arr("name", user.fullname),
      Json.arr("tel", user.phone),
      Json.arr("city", user.city),
      Json.arr("zipcode", user.postcode),
      Json.arr("addr", user.address)
    )
  )

  val loginFailResponse = Json.obj(
    "actions" -> Json.arr("flash-error"),
    "error"   -> "登入失敗，請檢查密碼"
  )

}