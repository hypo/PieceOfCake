package controllers

import models._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models.PieceJsonReaders._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global


object OrderController extends Controller {
  import scala.slick.driver.PostgresDriver.simple._
  import play.api.db.DB
  import play.api.Play.current
  
  lazy val db = Database.forDataSource(DB.getDataSource())

  def makeOrder = Action(parse.json(maxLength = 2 * 1024 * 1024)) { request =>
    Logger.info("receive: " + request.body)
    Json.fromJson[Piece](request.body).fold(
      error ⇒ {
        BadRequest("Json format invalid")
      },
      (piece: Piece) ⇒ Async { /* Success */
        future {
          Ok(Json.obj("token" -> piece.token))
        }
      }
    )
  }
  
  def showOrder(orderNumber: String) = Action {
    Ok("")
  }
}