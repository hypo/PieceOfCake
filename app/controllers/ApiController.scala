package controllers

import client.LiteClient
import client.LiteObjects._
import models.{PricingStrategy, PiecesPricingStrategy}
import models.PricingStrategyJSONFormatter._
import play.api.Play
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import util.ModelUtils

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

object ApiController extends Controller {
  import scala.slick.driver.PostgresDriver.simple._
  import play.api.Play.current
  import models.PiecesDAO._
  import APIControllerHelper._
  import ModelUtils._

  val config = Play.current.configuration
  val liteClient = new LiteClient(
    (config.getString("lite.username").get, config.getString("lite.password").get),
    config.getString("lite.baseurl").get
  )

  def start() = Action { request =>
    Found("/mobile").withSession(
      "cake_token" -> request.getQueryString("token").get
    )
  }

  def login() = Action.async { implicit request =>
    val credentials = for (
      params <- request.body.asFormUrlEncoded;
      emails <- params.get("email");
      passwords <- params.get("password")
    ) yield (emails.head, passwords.head)

    credentials map { case (email, password) =>
      liteClient.login(email, password) map {
        case Some(LoginResponse("ok", "verified", user)) => Ok(loginOkResponse(user.get)).addingToSession(
          "user_id" -> user.get.id.toString
        )
        case _ => Ok(loginFailResponse)
      }
    } getOrElse Future { BadRequest(loginFailResponse) }
  }

  def signup() = Action.async { implicit request =>
    LiteUserFromParams(request) map { user =>
      liteClient.signup(user) map {
        case Some(LoginResponse("ok", "created", user)) => Ok(loginOkResponse(user.get)).addingToSession(
          "user_id" -> user.get.id.toString
        )
        case _ => Ok(signupFailResponse)
      }
    } getOrElse(errorResponse("請確認欄位皆已填寫。"))
  }

  def createOrder() = Action.async { implicit request =>
    val sessionInfo = for {
      orderToken <- request.session.get("cake_token");
      userId <- request.session.get("user_id").map(_.toInt)
    } yield (orderToken, userId)

    sessionInfo map { case (orderToken, userId) =>
      getPiecesCount(orderToken) flatMap { piecesCount =>
        val pricingStrategy = PiecesPricingStrategy.copy(pieces_qty = piecesCount)
        LiteOrderFromParams(request, userId, orderToken, pricingStrategy) map { order =>
          liteClient.createOrder(order) map {
            case Some(OrderResponse("ok", "created", Some(order))) =>
              Ok(orderOkResponse(order.id.get)).addingToSession(
                "order_id" -> order.id.get.toString
              )
            case _ => Ok(orderFailResponse)
          }
        } getOrElse(errorResponse("請確認欄位皆已填寫。"))
      }
    } getOrElse(errorResponse("請確認欄位皆已填寫。"))
  }

  def creditCard() = Action.async { request =>
    request.body.asFormUrlEncoded match {
      case Some(params) => {
        if (requiredCreditCardFieldsArePresent(params)) {
          val p = extractParamHead(params)(_)
          val (orderId, card_no, expiry, cvv) = (request.session.get("order_id").get, p("card_no"), p("expiry"), p("cvv"))

          LiteClient.mkCard(card_no, expiry, cvv) match {
            case Some(creditCard) => liteClient.creditCard(orderId.toInt, creditCard) flatMap {
              case Some(CreditCardResponse("ok", _)) => Future { Ok(creditCardOkResponse) }
              case _ => errorResponse("刷卡失敗，請檢查信用卡資訊。")
            }
            case None => errorResponse("信用卡資訊無法識別。")
          }
        } else {
          errorResponse("請確認欄位皆已填寫。")
        }
      }
      case None => errorResponse("系統錯誤，請稍候再試。")
    }
  }

  def pricingStrategy() = Action.async { request =>
    val ps: Future[PricingStrategy] = request.session.get("cake_token") match {
      case Some(orderToken) =>
        getPiecesCount(orderToken) map { count =>
          PiecesPricingStrategy.copy(pieces_qty = count)
        }
      case None =>
        Future { PiecesPricingStrategy }
    }

    ps map { pricingStrategy =>
      request.getQueryString("callback") match {
        case Some(cb) => Ok(s"${cb}(${Json.toJson(pricingStrategy)})")
        case None     => Ok(Json.toJson(pricingStrategy))
      }
    }
  }

  def estimatedShippingDate() = Action.async { request =>
    liteClient.estimatedShippingDate map { r =>
      val estimatedShippingDateString = r.get.estimated_shipping_date

      request.getQueryString("callback") match {
        case Some(cb) => Ok(s"${cb}('${estimatedShippingDateString}')")
        case None     => Ok(estimatedShippingDateString)
      }
    }
  }

  private[this] def getPiecesCount(orderNumber: String) = Future {
    DB.withSession { implicit session =>
      Pieces.filter(p => p.token === orderNumber).firstOption.map(p => p.sheets.foldLeft(0)(_ + _.qty))
    }
  }

  def errorResponse(msg: String) = Future { Ok(mkError(msg)) }
}

object APIControllerHelper {
  def mkError(msg: String) = Json.obj(
    "actions" -> Json.arr("flash-error"),
    "error"   -> msg
  )

  def requiredSignupFieldsArePresent = verifyFieldsArePresent(Seq("name", "tel", "email", "password")) _
  def requiredOrderFieldsArePresent = verifyFieldsArePresent(Seq("name", "email", "tel", "country", "area", "city", "zipcode", "addr", "frame_qty")) _
  def requiredCreditCardFieldsArePresent = verifyFieldsArePresent(Seq("card_no", "expiry", "cvv", "order_id")) _

  def verifyFieldsArePresent(fields: Seq[String])(params: Map[String, Seq[String]]) = fields forall { key =>
    (params.getOrElse(key, Seq()).size > 0) && (params.get(key).get.head.trim.length > 0)
  }

  def extractParamHead(params: Map[String, Seq[String]])(key: String) : String = params.get(key).get.head.trim

  def loginOkResponse(user: LiteUser) = Json.obj(
    "actions" -> Json.arr("push-data", "push-card"),
    "card"    -> "card_shipping",
    "changes" -> Json.arr(
      Json.arr("name", user.fullname),
      Json.arr("email", user.email),
      Json.arr("tel", user.phone),
      Json.arr("area", user.state.getOrElse("").toString),
      Json.arr("city", user.city.getOrElse("").toString),
      Json.arr("zipcode", user.postcode.getOrElse("").toString),
      Json.arr("addr", user.address),
      Json.arr("country", user.country_id)
    )
  )

  def orderOkResponse(orderId: Int) = Json.obj(
    "actions" -> Json.arr("push-data", "push-card"),
    "card"    -> "card_credit_card",
    "changes" -> Json.arr(
      Json.arr("order_id", orderId)
    )
  )

  def creditCardOkResponse = Json.obj(
    "actions" -> Json.arr("push-card"),
    "card" -> "card_done"
  )

  val loginFailResponse = mkError("登入失敗，請檢查密碼")
  val signupFailResponse = mkError("註冊失敗，請稍候再試")
  val orderFailResponse = mkError("下單失敗，請稍候再試")
}