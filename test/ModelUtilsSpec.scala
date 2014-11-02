import client.LiteObjects.LiteCoupon
import models.PiecesPricingStrategy
import org.specs2.mutable._
import play.api.test.Helpers._
import play.api.test._
import util.ModelUtils

class ModelUtilsSpec extends Specification {
  
  "ModelUtils" should {
    
    "#LiteOrderFromParams should work" in {
      import ModelUtils._

      val req = FakeRequest("GET", "/api/create_order").withFormUrlEncodedBody(
        "name" -> "1",
        "email" -> "2",
        "tel" -> "3",
        "country" -> "4",
        "area" -> "5",
        "city" -> "6",
        "zipcode" -> "7",
        "addr" -> "8",
        "frame_qty" -> "10"
      )

      val order = LiteOrderFromParams(req, 0, "token", PiecesPricingStrategy.pricingStrategyFor(Some(5)), LiteCoupon(can_redeem = false))
      order.isDefined must equalTo(true)
      order.get.fullname must equalTo("1")
      order.get.email must equalTo("2")
      order.get.phone must equalTo("3")
      order.get.country_id must equalTo(4)
      order.get.state must equalTo("5")
      order.get.city must equalTo("6")
      order.get.postcode must equalTo("7")
      order.get.address must equalTo("8")
    }

  }
}