package models

import play.api.libs.json._
import play.api.libs.functional.syntax._

case class PricingStrategy(
  pieces: Int,
  shipping: Int,
  frame: Int
) {
  def total(frame_qty: Int) = pieces + shipping + frame * frame_qty
}

object PiecesPricingStrategy extends PricingStrategy(390, 0, 390)

object PricingStrategyReader {
  import play.api.libs.json.Reads._

  implicit val pricingStrategyRead: Reads[PricingStrategy] = (
    (__ \ "pieces").read[Int] ~
    (__ \ "shipping").read[Int] ~
    (__ \ "frame").read[Int]
  )(PricingStrategy)
}

object PricingStrategyWriter {
  import play.api.libs.json.Writes._

  implicit val pricingStrategyWrite: Writes[PricingStrategy] = (
    (__ \ "pieces").write[Int] ~
    (__ \ "shipping").write[Int] ~
    (__ \ "frame").write[Int]
  )(unlift(PricingStrategy.unapply))
}

