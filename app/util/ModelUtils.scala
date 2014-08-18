package util

import client.LiteObjects.{LiteOrder, LiteUser}
import models.PricingStrategy
import play.api.mvc.{AnyContent, Request}

object ModelUtils {
  def filterFirstParam(params: Map[String, Seq[String]]) : Map[String, String] = {
    params.map { case (k, v) =>
      (k, v.head)
    }
  }

  def LiteUserFromParams(request: Request[AnyContent]): Option[LiteUser] = {
    for {
      params <- request.body.asFormUrlEncoded map { filterFirstParam(_) };
      name <- params.get("name");
      password <- params.get("password");
      tel <- params.get("tel");
      email <- params.get("email")
    } yield LiteUser(
      fullname = Some(name),
      phone = tel,
      email = email,
      password = Some(password)
    )
  }

  def LiteOrderFromParams(request: Request[AnyContent], userId: Int, orderToken: String, pricingStrategy: PricingStrategy): Option[LiteOrder] = {
    for {
      params <- request.body.asFormUrlEncoded map { filterFirstParam(_) };
      name <- params.get("name");
      email <- params.get("email");
      tel <- params.get("tel");
      country <- params.get("country");
      area <- params.get("area");
      city <- params.get("city");
      zipcode <- params.get("zipcode");
      addr <- params.get("addr");
      total <- pricingStrategy.copy(frame_qty = params.get("frame_qty").map(_.toInt)).total
    } yield LiteOrder(
      user_id = userId,
      total = total,
      number_of_pages = 1,
      email = email,
      fullname = name, phone = tel,
      city = city, state = area,
      postcode = zipcode, address = addr,
      token = orderToken
    )
  }
}
