package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class PricingStrategy(
  pieces_price: Int,
  shipping_fee: Int,
  frame_price:  Int
)

object PiecesPricingStrategy extends PricingStrategy(390, 0, 390)

object PricingStrategyReader {
  import play.api.libs.json.Reads._

  implicit val pricingStrategyRead: Reads[PricingStrategy] = (
    (__ \ "pieces_price").read[Int] ~
    (__ \ "shipping_fee").read[Int] ~
    (__ \ "frame_price").read[Int]
  )(PricingStrategy)
}

object PricingStrategyWriter {
  import play.api.libs.json.Writes._

  implicit val pricingStrategyWrite: Writes[PricingStrategy] = (
    (__ \ "pieces_price").write[Int] ~
    (__ \ "shipping_fee").write[Int] ~
    (__ \ "frame_price").write[Int]
  )(unlift(PricingStrategy.unapply))
}

