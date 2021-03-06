package controllers

import java.io.File
import scala.collection.JavaConverters._

import models._
import org.pac4j.oauth.profile.twitter.TwitterProfile

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.db.slick.DB
import models.PieceJsonReaders._

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

import org.pac4j.play.scala._

object OrderController extends ScalaController {
  import scala.slick.driver.PostgresDriver.simple._
  import play.api.Play.current
  import models.PiecesDAO._

  def pdfPathForToken(token: String): String =
    Play.getFile("pdfs").getAbsolutePath + "/" + token + ".pdf"

  def thumbnailPathForToken(token: String, sheetIndex: Int): String =
    Play.getFile("thumbnails").getAbsolutePath + "/" + token + "_" + sheetIndex + ".png"

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

  def thumbnail(orderToken: String, sheetIndex: Int = 0) = Action.async { implicit request =>
    val jpgFile = new File(thumbnailPathForToken(orderToken, sheetIndex))
    if (jpgFile.exists()) {
      Future {
        Ok.sendFile(content = jpgFile, inline = true)
      }
    } else {
      DB.withSession { implicit session =>
        Pieces.filter(p => p.token === orderToken).firstOption.flatMap(p => {
          java.nio.file.Files.createDirectories(jpgFile.toPath.getParent)
          p.sheets.lift(sheetIndex)
        }).map(sheet => 
          sheet.thumbnail(jpgFile.getAbsolutePath).map(img => 
            Ok.sendFile(content = jpgFile, inline = true)
          )
        ).getOrElse(
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

  def list(page: Int = 0, pageSize: Int = 50) = RequiresAuthentication("TwitterClient") { profile =>
    Action.async { implicit request =>
      Future {
        val twitterProfile = profile.asInstanceOf[TwitterProfile]
        val whitelist: Set[String]= Play.current.configuration.getStringList("twitter.allowed_account").get.asScala.toSet
        if (whitelist(twitterProfile.getUsername)) {
          val p = PiecesDAO.list(page, pageSize)
          Ok(views.html.list(p))
        } else {
          Unauthorized("Unauthorized user.")
        }
      }
    }

  }
}