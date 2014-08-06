package client

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.Reads._

package object LiteObjects {

  case class AccessToken(token_type: String, access_token: String) {
    override def toString = s"$token_type $access_token"
  }

  implicit val accessTokenRead: Reads[AccessToken] = (
    (__ \ "token_type").read[String] ~
    (__ \ "access_token").read[String]
  )(AccessToken)

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

  implicit val liteUserRead: Reads[LiteUser] = (
    (__ \ "id").read[Int] ~
    (__ \ "email").read[String] ~
    (__ \ "pasword").readNullable[String] ~
    (__ \ "fullname").readNullable[String] ~
    (__ \ "country_id").read[Int] ~
    (__ \ "structure_id").read[Int] ~
    (__ \ "state").readNullable[String] ~
    (__ \ "city").readNullable[String] ~
    (__ \ "postcode").readNullable[String] ~
    (__ \ "address").read[String] ~
    (__ \ "phone").read[String]
  )(LiteUser)

  implicit val liteUserWrite: Writes[LiteUser] = (
    (__ \ "id").write[Int] ~
    (__ \ "email").write[String] ~
    (__ \ "password").writeNullable[String] ~
    (__ \ "fullname").writeNullable[String] ~
    (__ \ "country_id").write[Int] ~
    (__ \ "structure_id").write[Int] ~
    (__ \ "state").writeNullable[String] ~
    (__ \ "city").writeNullable[String] ~
    (__ \ "postcode").writeNullable[String] ~
    (__ \ "address").write[String] ~
    (__ \ "phone").write[String]
  )(unlift(LiteUser.unapply))

  case class LiteOrder(
    id: Option[Int] = None,
    user_id: Int,
    total: Int,
    numberOfPages: Int,
    email: String,
    fullname: String,
    phone: String,
    country_id: Int = 1,
    city: String,
    state: String,
    postcode: String,
    address: String
  )

  implicit val liteOrderRead: Reads[LiteOrder] = (
    (__ \ "id").readNullable[Int] ~
    (__ \ "user_id").read[Int] ~
    (__ \ "total").read[Int] ~
    (__ \ "number_of_pages").read[Int] ~
    (__ \ "email").read[String] ~
    (__ \ "fullname").read[String] ~
    (__ \ "phone").read[String] ~
    (__ \ "country_id").read[Int] ~
    (__ \ "city").read[String] ~
    (__ \ "state").read[String] ~
    (__ \ "postcode").read[String] ~
    (__ \ "address").read[String]
  )(LiteOrder)

  implicit val liteOrderWrite: Writes[LiteOrder] = (
    (__ \ "id").writeNullable[Int] ~
    (__ \ "user_id").write[Int] ~
    (__ \ "total").write[Int] ~
    (__ \ "number_of_pages").write[Int] ~
    (__ \ "email").write[String] ~
    (__ \ "fullname").write[String] ~
    (__ \ "phone").write[String] ~
    (__ \ "country_id").write[Int] ~
    (__ \ "city").write[String] ~
    (__ \ "state").write[String] ~
    (__ \ "postcode").write[String] ~
    (__ \ "address").write[String]
  )(unlift(LiteOrder.unapply))

  case class Response(status: String, message: String)

  implicit val responseRead: Reads[Response] = (
    (__ \ "status").read[String] ~
    (__ \ "message").read[String]
  )(Response)

  case class LoginResponse(status: String, message: String, user: Option[LiteUser])

  implicit val loginResponseRead: Reads[LoginResponse] = (
    (__ \ "status").read[String] ~
    (__ \ "message").read[String] ~
    (__ \ "user").readNullable[LiteUser]
  )(LoginResponse)

  case class OrderResponse(status: String, message: String, order: Option[LiteOrder])

  implicit val orderResponseRead: Reads[OrderResponse] = (
    (__ \ "status").read[String] ~
    (__ \ "message").read[String] ~
    (__ \ "order").readNullable[LiteOrder]
  )(OrderResponse)

  case class ATMAccountResponse(status: String, message: String, account: String)

  implicit val atmAccountResponseRead: Reads[ATMAccountResponse] = (
    (__ \ "status").read[String] ~
    (__ \ "message").read[String] ~
    (__ \ "account").read[String]
  )(ATMAccountResponse)

  object Constants {
    val CountryIdToName = Map(
      1 -> "Taiwan",
      3 -> "Hong Kong",
      5 -> "Macao",
      9 -> "Singapore",
      10 -> "Malaysia",
      11 -> "Japan",
      12 -> "Australia",
      13 -> "Netherlands",
      14 -> "USA",
      15 -> "UK",
      16 -> "Canada",
      17 -> "France",
      18 -> "South Korea",
      19 -> "Finland",
      20 -> "Spain",
      21 -> "South Africa",
      22 -> "Thailand",
      23 -> "China",
      24 -> "Switzerland",
      25 -> "Sweden",
      26 -> "Turkey",
      27 -> "New Zealand",
      28 -> "Germany",
      31 -> "PengHu",
      32 -> "JinMen",
      33 -> "Czech Republic",
      34 -> "Brazil",
      35 -> "Mazu",
      36 -> "Russia",
      37 -> "India",
      38 -> "Poland",
      39 -> "United Arab Emirates",
      40 -> "Greece",
      42 -> "Norway"
    )
  }
}
