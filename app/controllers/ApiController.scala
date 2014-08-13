package controllers

import client.LiteClient
import client.LiteObjects._
import com.github.t3hnar.bcrypt._
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
        case Some(LoginResponse("ok", "verified", user)) => Ok(Json.stringify(loginOkResponse(user.get))).withSession(
          "user_id" -> user.get.id.toString
        )
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
            case Some(LoginResponse("ok", "created", user)) => Ok(Json.stringify(loginOkResponse(user.get))).withSession(
              "user_id" -> user.get.id.toString
            )
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
            name, email, tel, country, area, city, zipcode, addr, frame_qty
          ) = (
            p("name"), p("email"), p("tel"), p("country"), p("area"), p("city"), p("zipcode"), p("addr"), p("frame_qty")
          )
          val price = PiecesPricingStrategy.total(frame_qty.toInt)

          val order = LiteOrder(
            user_id  = request.session.get("user_id").getOrElse("0").toInt,
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

          liteClient.createOrder(order) flatMap {
            case Some(OrderResponse("ok", "created", order)) =>
              val orderId: Option[Int] = for (
                o <- order;
                id <- o.id
              ) yield id

              orderId match {
                case Some(orderId) => Future { Ok(Json.stringify(orderOkResponse(orderId))) }
                case None => Future { Ok(Json.stringify(orderFailResponse)) }
              }
            case _ => Future { Ok(Json.stringify(orderFailResponse)) }
          }
        } else {
          errorResponse("請確認欄位皆已填寫。")
        }
      }
      case None => errorResponse("系統錯誤，請稍候再試。")
    }
  }

  def creditCard() = Action.async { request =>
    request.body.asFormUrlEncoded match {
      case Some(params) => {
        if (requiredCreditCardFieldsArePresent(params)) {
          if (verifyCCSignature(params)) {
            val p = extractParamHead(params)(_)
            val (orderId, card_no, expiry, cvv) = (p("order_id"), p("card_no"), p("expiry"), p("cvv"))

            LiteClient.mkCard(card_no, expiry, cvv) match {
              case Some(creditCard) => liteClient.creditCard(orderId.toInt, creditCard) map { d => Ok(Json.stringify(creditCardOkResponse))}
              case None => errorResponse("信用卡資訊無法識別。")
            }
          } else {
            errorResponse("系統錯誤，請稍候再試。")
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

  def estimatedShippingDate() = Action.async { request =>
    liteClient.estimatedShippingDate map { r =>
      val estimatedShippingDateString = r.get.estimated_shipping_date

      request.getQueryString("callback") match {
        case Some(cb) => Ok(s"${cb}('${estimatedShippingDateString}')")
        case None     => Ok(estimatedShippingDateString)
      }
    }
  }

  private[this] def mkError(msg: String) = Json.obj(
    "actions" -> Json.arr("flash-error"),
    "error"   -> msg
  )

  private[this] def errorResponse(msg: String) = Future { Ok(mkError(msg)) }

  private[this] def requiredSignupFieldsArePresent = verifyFieldsArePresent(Seq("name", "tel", "email", "password")) _

  private[this] def requiredOrderFieldsArePresent = verifyFieldsArePresent(Seq("name", "email", "tel", "country", "area", "city", "zipcode", "addr", "frame_qty")) _

  private[this] def verifyCCSignature(params: Map[String, Seq[String]]) = {
    creditCardSignString(params.get("order_id").get.head.toInt).isBcrypted(params.get("sig").get.head.trim)
  }

  private[this] def requiredCreditCardFieldsArePresent = verifyFieldsArePresent(Seq("card_no", "expiry", "cvv", "order_id", "sig")) _

  private[this] def verifyFieldsArePresent(fields: Seq[String])(params: Map[String, Seq[String]]) = fields forall { key =>
    (params.getOrElse(key, Seq()).size > 0) && (params.get(key).get.head.trim.length > 0)
  }

  private[this] def extractParamHead(params: Map[String, Seq[String]])(key: String) : String = params.get(key).get.head.trim

  private[this] def loginOkResponse(user: LiteUser) = Json.obj(
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

  private[this] def creditCardSignString(orderId: Int) = s"hypo-Pieces-credit-card-token-27dh13uhd0918hu9dh1278-$orderId"
  private[this] def orderOkResponse(orderId: Int) = Json.obj(
    "actions" -> Json.arr("push-data", "push-card"),
    "card"    -> "card_credit_card",
    "changes" -> Json.arr(
      Json.arr("order_id", orderId),
      Json.arr("cc_sig", creditCardSignString(orderId).bcrypt)
    )
  )

  private[this] def creditCardOkResponse = Json.obj(
    "actions" -> Json.arr("push-card"),
    "card" -> "card_done"
  )

  private[this] val loginFailResponse = mkError("登入失敗，請檢查密碼")
  private[this] val signupFailResponse = mkError("註冊失敗，請稍候再試")
  private[this] val orderFailResponse = mkError("下單失敗，請稍候再試")

}