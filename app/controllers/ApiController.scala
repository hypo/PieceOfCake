package controllers

import client.LiteClient
import client.LiteObjects.LoginResponse
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
        case Some(LoginResponse("ok", "verified", _)) => Ok(Json.stringify(loginOkResponse))
        case _ => BadRequest(Json.stringify(loginFailResponse))
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

  val loginOkResponse = Json.obj(
    "actions" -> Json.arr("push-card"),
    "card"    -> "card_shipping"
  )

  val loginFailResponse = Json.obj(
    "actions" -> Json.arr("flash-error"),
    "error"   -> "登入失敗，請檢查密碼"
  )

}