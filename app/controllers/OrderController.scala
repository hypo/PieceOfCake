package controllers

import models._

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models.PieceJsonReaders._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.lifted._
import Q.interpolation
import Database.threadLocalSession

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
          db withSession {
            Pieces.forInsert.insert(piece)
          }
          Ok(Json.obj("token" -> piece.token))
        }
      }
    )
  }
  
  def showOrder(orderNumber: String) = Action { implicit request =>
    Async {
      future {
        db withSession {
          val q = for (p <- Pieces if p.token === orderNumber) yield p
          q.firstOption.map(p =>
            Ok(views.html.order(p))
          ).getOrElse(NotFound("Order Not Found"))
        }
      }
    }
  }

  def list = Action { implicit request =>
    Async {
      future {
        db withSession {
          val q = Query(Pieces)
          Ok(views.html.list(q.list))
        }
      }
    }
  }
}