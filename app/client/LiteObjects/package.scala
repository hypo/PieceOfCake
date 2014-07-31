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

  case class LiteUser(id: Int, email: String, fullname: String, country_id: Int, city: String, postcode: String, address: String, phone: String)

  implicit val liteUserRead: Reads[LiteUser] = (
    (__ \ "id").read[Int] ~
    (__ \ "email").read[String] ~
    (__ \ "fullname").read[String] ~
    (__ \ "country_id").read[Int] ~
    (__ \ "city").read[String] ~
    (__ \ "postcode").read[String] ~
    (__ \ "address").read[String] ~
    (__ \ "phone").read[String]
  )(LiteUser)

  case class LoginResponse(status: String, message: String, user: Option[LiteUser])

  implicit val loginResponseRead: Reads[LoginResponse] = (
    (__ \ "status").read[String] ~
    (__ \ "message").read[String] ~
    (__ \ "user").readNullable[LiteUser]
  )(LoginResponse)

}
