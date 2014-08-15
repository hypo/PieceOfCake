package controllers

import models._

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.data._
import play.api.data.Forms._
import play.api.db.slick.DB
import play.api.data.validation.Constraints._
import models.PieceJsonReaders._


import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.slick.driver.PostgresDriver.simple._

object OrderController extends Controller {
  import scala.slick.driver.PostgresDriver.simple._
  import play.api.Play.current
  import models.PiecesDAO._

  def makeOrder = Action.async(parse.tolerantJson(maxLength = 2 * 1024 * 1024)) { request =>
    Logger.info("receive: " + request.body)
    Json.fromJson[Piece](request.body).fold(
      error ⇒ Future { 
        BadRequest("Json format invalid")
      },
      (piece: Piece) ⇒ Future {
        DB.withSession { implicit session =>
          Pieces.insert(piece)
        }
        Ok(Json.obj("token" -> piece.token))
      }
    )
  }

  def showOrder(orderToken: String) = Action.async { implicit request =>
    Future {
      DB.withSession { implicit session =>
        Pieces.filter(p => p.token === orderToken).firstOption.map(p =>
          Ok(views.html.order(p))
        ).getOrElse(
          NotFound("Not Found!")
        )
      }
    }
  }

  def showPCD(orderToken: String) = Action.async { implicit request =>
    Future {
      DB.withSession { implicit session =>
        Pieces.filter(p => p.token === orderToken).firstOption.map(p =>
          Ok(p.sheets.map(s => (s.pcdString + "\n") * s.qty).mkString("#############\n"))
        ).getOrElse(
          NotFound("Order Not Found")
        )
      }
    }
  }

  def list = Action.async { implicit request =>
    Future {
      DB.withSession { implicit session =>
        Ok(views.html.list(Pieces.list))
      }
    }
  }
}