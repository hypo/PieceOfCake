package client

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

package object LiteObjects {
  case class AccessToken(token_type: String, access_token: String) {
    override def toString = s"$token_type $access_token"
  }
  implicit val accessTokenRead: Reads[AccessToken] = Json.reads[AccessToken]

  case class LiteUser(
    id: Int = -1,
    email: String = "",
    password: Option[String] = None,
    fullname: Option[String] = None,
    country_id: Int = 1,
    structure_id: Int = 1,
    state: Option[String] = None,
    city: Option[String] = None,
    postcode: Option[String] = None,
    address: String = "",
    phone: String = ""
  )
  implicit val liteUserRead: Reads[LiteUser] = Json.reads[LiteUser]
  implicit val liteUserWrite: Writes[LiteUser] = Json.writes[LiteUser]

  case class LiteOrder(
    id: Option[Int] = None,
    user_id: Int,
    total: Int,
    number_of_pages: Int,
    email: String,
    fullname: String,
    phone: String,
    country_id: Int = 1,
    city: String,
    state: String,
    postcode: String,
    address: String,
    token: String,
    coupon_id: Option[Int] = None
  )
  implicit val liteOrderRead: Reads[LiteOrder] = Json.reads[LiteOrder]
  implicit val liteOrderWrite: Writes[LiteOrder] = Json.writes[LiteOrder]

  case class LiteCoupon(
    id: Int = -1,
    code: String = "",
    created_at: Long = 0,
    expired_at: Long = 0,
    multiple_redeem: Boolean = false,
    can_redeem: Boolean = false,
    discount_amount: Option[Int] = None,
    percent_off: Option[Int] = None
  ) {
    def discountedPrice(price: Int) : Int = {
      if (can_redeem) {
        if (discount_amount.isDefined)
          price - discount_amount.get
        else if (percent_off.isDefined)
          price * (100 - percent_off.get) / 100
        else
          price
      } else price
    }
  }
  implicit val liteCouponRead: Reads[LiteCoupon] = Json.reads[LiteCoupon]
  implicit val liteCouponWrite: Writes[LiteCoupon] = Json.writes[LiteCoupon]

  case class Response(status: String, message: String)
  implicit val responseRead: Reads[Response] = Json.reads[Response]

  case class LoginResponse(status: String, message: String, user: Option[LiteUser])
  implicit val loginResponseRead: Reads[LoginResponse] = Json.reads[LoginResponse]

  case class OrderResponse(status: String, message: String, order: Option[LiteOrder])
  implicit val orderResponseRead: Reads[OrderResponse] = Json.reads[OrderResponse]

  case class ATMAccountResponse(status: String, message: String, account: String)
  implicit val atmAccountResponseRead: Reads[ATMAccountResponse] = Json.reads[ATMAccountResponse]

  case class EstimatedShippingDateResponse(status: String, estimated_shipping_date: String)
  implicit val estimatedShippingDateResponseRead: Reads[EstimatedShippingDateResponse] = Json.reads[EstimatedShippingDateResponse]

  case class CreditCardResponse(status: String, message: String /* and a bunch of stuff that I don't care right now */)
  implicit val creditCardResponseRead: Reads[CreditCardResponse] = Json.reads[CreditCardResponse]

  case class QueryCouponResponse(status: String, message: String, coupon: Option[LiteCoupon])
  implicit val queryCouponResponseRead: Reads[QueryCouponResponse] = Json.reads[QueryCouponResponse]
  implicit val queryCouponResponseWrite: Writes[QueryCouponResponse] = Json.writes[QueryCouponResponse]
}
