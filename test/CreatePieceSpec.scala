package test

import org.specs2.mutable._
import play.api.libs.ws._
import play.api.test.Helpers._
import play.api.test._
import play.api.libs.json._
import play.api.db.DB
import play.api._
import play.api.Play.current

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.lifted._
import Q.interpolation

import models._
import controllers._

class CreatePieceSpec extends Specification with RequestMaker {
  val exampleJson = """{
  "pieces_type":"test",
  "data": [
    {
      "qty":1,
      "photos":[
        {
          "source":"instagram",
          "url":"http://distilleryimage4.s3.amazonaws.com/11e00ae29cff11e2b23122000a1f98cf_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage9.s3.amazonaws.com/d279de22a2b811e2855722000aa800e1_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage9.s3.amazonaws.com/d279de22a2b811e2855722000aa800e1_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage9.s3.amazonaws.com/d279de22a2b811e2855722000aa800e1_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage9.s3.amazonaws.com/d279de22a2b811e2855722000aa800e1_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage9.s3.amazonaws.com/d279de22a2b811e2855722000aa800e1_7.jpg"
        }
      ]
    },
    {
      "qty":2,
      "photos":[
        {
          "source":"instagram",
          "url":"http://distilleryimage0.s3.amazonaws.com/5a7f2092a29711e28d8c22000a1f9ad6_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage0.s3.amazonaws.com/5a7f2092a29711e28d8c22000a1f9ad6_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage0.s3.amazonaws.com/5a7f2092a29711e28d8c22000a1f9ad6_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage0.s3.amazonaws.com/5a7f2092a29711e28d8c22000a1f9ad6_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage0.s3.amazonaws.com/5a7f2092a29711e28d8c22000a1f9ad6_7.jpg"
        },
        {
          "source":"instagram",
          "url":"http://distilleryimage0.s3.amazonaws.com/5a7f2092a29711e28d8c22000a1f9ad6_7.jpg"
        }
      ]
    }
  ]
}
"""

  val badJsonFormatRequest = localJsonRequest(POST, "/piece", "{")
  val formatInvalidRequest = localJsonRequest(POST, "/piece", """{"data":[]}""")
  val successRequest = FakeRequest(POST, "/piece").withBody(Json.parse(exampleJson)).withHeaders("Content-Type"->"application/json")

  "Create Piece" should {
    "return '400 Bad Request' if the request body is not a json" in running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val badrequest = route(badJsonFormatRequest).get      
      status(badrequest) must equalTo(BAD_REQUEST)
    }
    
    "return 400 Bad Request if the json miss some fields" in running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val formatInvalid = route(formatInvalidRequest).get
      status(formatInvalid) must equalTo(BAD_REQUEST)
    }

    "return a token if the request body is a valid piece json"  in running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
      val success = route(successRequest).get
      status(success) must equalTo(OK)
      val json = Json.parse(contentAsString(success))
      (json \ "token").asOpt[String] must beSome[String].which(_.length > 0)
    }
  }

}