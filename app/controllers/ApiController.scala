package controllers

import models.PiecesPricingStrategy
import models.PricingStrategyWriter._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

object ApiController extends Controller {

  def pricingStrategy() = Action { request =>
    val jsonObj = Json.toJson(PiecesPricingStrategy)

    request.getQueryString("callback") match {
      case Some(cb) => Ok(s"${cb}(${jsonObj})")
      case None     => Ok(jsonObj)
    }
  }

}
