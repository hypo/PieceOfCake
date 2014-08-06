package controllers

import client.LiteClient
import client.LiteObjects._
import models.PiecesPricingStrategy
import models.PricingStrategyWriter._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

object ApiController extends Controller {

  val liteClient = new LiteClient()

  def login() = Action.async { request =>
    val credentials = for (
      params <- request.body.asFormUrlEncoded;
      emails <- params.get("email");
      passwords <- params.get("password")
    ) yield (emails.head, passwords.head)

    credentials map { case (email, password) =>
      liteClient.login(email, password) map {
        case Some(LoginResponse("ok", "verified", user)) => Ok(Json.stringify(loginOkResponse(user.get)))
        case _ => Ok(Json.stringify(loginFailResponse))
      }
    } getOrElse Future { BadRequest(Json.stringify(loginFailResponse)) }
  }

  def signup() = Action.async { request =>
    request.body.asFormUrlEncoded match {
      case Some(params) => {
        if (requiredSignupFieldsArePresent(params)) {
          val p = extractParamHead(params)(_)
          val (name, tel, email, password) = (p("name"), p("tel"), p("email"), p("password"))

          val user = LiteUser(fullname = Some(name), phone = tel, email = email, password = Some(password))
          liteClient.signup(user) map {
            case Some(LoginResponse("ok", "created", user)) => Ok(Json.stringify(loginOkResponse(user.get)))
            case _ => Ok(Json.stringify(signupFailResponse))
          }
        } else {
          errorResponse("請確認欄位皆已填寫。")
        }
      }
      case None => errorResponse("系統錯誤，請稍候再試。")
    }
  }

  def createOrder() = Action.async { request =>
    request.body.asFormUrlEncoded match {
      case Some(params) => {
        if (requiredOrderFieldsArePresent(params)) {
          val p = extractParamHead(params)(_)
          val (
            name, email, tel, country, area, city, zipcode, addr, frame_qty, price
          ) = (
            p("name"), p("email"), p("tel"), p("country"), p("area"), p("city"), p("zipcode"), p("addr"), p("frame_qty"), p("price")
          )

          val order = LiteOrder(
            user_id = 0,
            total    = price.toInt,
            numberOfPages = 1,
            email    = email,
            fullname = name,
            phone    = tel,
            city     = city,
            state    = area,
            postcode = zipcode,
            address  = addr
          )

          liteClient.createOrder(order) map {
            case Some(OrderResponse("ok", "created", order)) =>
              val orderId: Option[Int] = for (
                o <- order;
                id <- o.id
              ) yield id

              orderId match {
                case Some(atmAccount) => Ok(Json.stringify(orderOkResponse))
                case None => Ok(Json.stringify(orderFailResponse))
              }
            case _ => Ok(Json.stringify(orderFailResponse))
          }
        } else {
          errorResponse("請確認欄位皆已填寫。")
        }
      }
      case None => errorResponse("系統錯誤，請稍候再試。")
    }
  }

  def pricingStrategy() = Action { request =>
    val jsonObj = Json.toJson(PiecesPricingStrategy)

    request.getQueryString("callback") match {
      case Some(cb) => Ok(s"${cb}(${jsonObj})")
      case None     => Ok(jsonObj)
    }
  }


  private[this] def mkError(msg: String) = Json.obj(
    "actions" -> Json.arr("flash-error"),
    "error"   -> msg
  )

  private[this] def errorResponse(msg: String) = Future { Ok(mkError(msg)) }

  private[this] def requiredSignupFieldsArePresent(params: Map[String, Seq[String]]) = Seq("name", "tel", "email", "password") forall { key =>
    (params.getOrElse(key, Seq()).size > 0) && (params.get(key).get.head.trim.length > 0)
  }

  private[this] def requiredOrderFieldsArePresent(params: Map[String, Seq[String]]) = Seq(
    "name", "tel", "country", "area", "city", "zipcode", "addr", "frame_qty", "price"
  ) forall { key =>
    (params.getOrElse(key, Seq()).size > 0) && (params.get(key).get.head.trim.length > 0)
  }

  private[this] def extractParamHead(params: Map[String, Seq[String]])(key: String) : String = params.get(key).get.head.trim

  private[this] def loginOkResponse(user: LiteUser) = Json.obj(
    "actions" -> Json.arr("push-data", "push-card"),
    "card"    -> "card_shipping",
    "changes" -> Json.arr(
      Json.arr("name", user.fullname),
      Json.arr("tel", user.phone),
      Json.arr("area", user.state.getOrElse("").toString),
      Json.arr("city", user.city.getOrElse("").toString),
      Json.arr("zipcode", user.postcode.getOrElse("").toString),
      Json.arr("addr", user.address),
      Json.arr("country", Constants.CountryIdToName.getOrElse(user.country_id, "").toString)
    )
  )

  private[this] def orderOkResponse = Json.obj(
    "actions" -> Json.arr("push-card"),
    "card"    -> "card_done"
  )
  private[this] val loginFailResponse = mkError("登入失敗，請檢查密碼")
  private[this] val signupFailResponse = mkError("註冊失敗，請稍候再試")
  private[this] val orderFailResponse = mkError("下單失敗，請稍候再試")

}