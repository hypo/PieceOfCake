package controllers

import java.io.File

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

object OrderController extends Controller {
  import scala.slick.driver.PostgresDriver.simple._
  import play.api.Play.current
  import models.PiecesDAO._

  def pdfPathForToken(token: String): String =
    Play.getFile("pdfs").getAbsolutePath + "/" + token + ".pdf"


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
        val pdfFile = new File(pdfPathForToken(piece.token))
        java.nio.file.Files.createDirectories(pdfFile.toPath.getParent)
        piece.pdfDocument(pdfFile.getAbsolutePath).map( f =>
          Logger.info(s"${piece.token} pdf done.")
        )
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

  def downloadPDF(orderToken: String) = Action.async { implicit request =>
    val pdfFile = new File(pdfPathForToken(orderToken))
    if (pdfFile.exists()) {
      Future {
        Ok.sendFile(content = pdfFile, inline = true)
      }
    } else {
      DB.withSession { implicit session =>
        Pieces.filter(p => p.token === orderToken).firstOption.map(p => {
          java.nio.file.Files.createDirectories(pdfFile.toPath.getParent)
          p.pdfDocument(pdfFile.getAbsolutePath).map( f =>
            Ok.sendFile(content = pdfFile, inline = true)
          )
        }).getOrElse(
            Future { NotFound("Order Not Found") }
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