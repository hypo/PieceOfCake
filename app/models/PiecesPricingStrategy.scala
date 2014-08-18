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

object PiecesPricingStrategy {
  def default: PricingStrategy = PricingStrategy(390, 0, 390, frame_qty = Some(0))

  def pricingStrategyFor(pieces_qty: Option[Int]): PricingStrategy = {
    val singlePricing = pieces_qty match {
      case Some(v) if (0 to 2).contains(v) => 490
      case Some(v) if (3 to 20).contains(v) => 450
      case Some(v) if (20 to 50).contains(v) => 390
      case Some(v) if (50 to Int.MaxValue).contains(v) => 350
      case _ => 0
    }
    PiecesPricingStrategy.default.copy(pieces = singlePricing, pieces_qty = pieces_qty)
  }
}

object PricingStrategyJSONFormatter {
  implicit val pricingStrategyRead: Reads[PricingStrategy] = Json.reads[PricingStrategy]
  implicit val pricingStrategyWrite: Writes[PricingStrategy] = Json.writes[PricingStrategy]
}