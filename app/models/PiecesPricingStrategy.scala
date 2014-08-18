package models

import play.api.libs.json._

case class PricingStrategy(
  pieces: Int,
  shipping: Int,
  frame: Int,
  pieces_qty: Option[Int] = None,
  frame_qty: Option[Int] = None
) {
  val total = for (
    pq <- pieces_qty;
    fq <- frame_qty
  ) yield (pieces * pq + shipping + frame * fq)
}

object PiecesPricingStrategy extends PricingStrategy(390, 0, 390)

object PricingStrategyJSONFormatter {
  implicit val pricingStrategyRead: Reads[PricingStrategy] = Json.reads[PricingStrategy]
  implicit val pricingStrategyWrite: Writes[PricingStrategy] = Json.writes[PricingStrategy]
}