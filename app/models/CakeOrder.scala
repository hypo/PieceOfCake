package models

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.lifted._
import Q.interpolation

import Database.threadLocalSession
import play.api.Play.current

import java.sql._

import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

case class Photo(source: String, url: String)
case class PieceOfSheet(qty: Int, photos: Seq[Photo])
case class Piece( id: Option[Int], 
                  pieceType: String,
                  sheets: Seq[PieceOfSheet], 
                  createdAt: Timestamp,
                  token: String) {

  def this(id: Option[Int], pieceType: String, sheets: Seq[PieceOfSheet]) = 
    this(id, pieceType, sheets, new Timestamp(new java.util.Date().getTime), java.util.UUID.randomUUID.toString)
}

object Pieces extends Table[Piece]("piece") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def pieceType = column[String]("type")
  def jsonData = column[String]("json_data")
  def createdAt = column[Timestamp]("created_at")
  def token = column[String]("token")


  implicit val photoReads: Reads[Photo] = (
    (__ \ "source").read[String] ~
    (__ \ "url").read[String]
  )(Photo)

  implicit val pieceOfSheetReads: Reads[PieceOfSheet] = (
    (__ \ "qty").read[Int] ~
    (__ \ "photos").read(list[Photo](photoReads))
  )(PieceOfSheet)

  implicit val pieceReads: Reads[Piece] = (
    (__ \ "pieces_type").read[String] ~
    (__ \  "data").lazyRead(list[PieceOfSheet](pieceOfSheetReads))
  )((pieceType, sheets) => new Piece(None, pieceType, sheets))


  def * = id.? ~ pieceType ~ jsonData ~ createdAt ~ token <> (
    (id, pieceType, jsonData, createdAt, token) ⇒
      Piece(id, pieceType, Json.fromJson[List[PieceOfSheet]](Json.parse(jsonData)).get, createdAt, token), 
    
    (p: Piece) ⇒ Some(p.id, p.pieceType, "{}", p.createdAt, p.token)
  )
}