package models

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import scala.slick.lifted._
import Q.interpolation

import Database.threadLocalSession
import play.api.Play.current

import java.sql._

import play.api.libs.json._
import play.api.libs.functional.syntax._

object PieceJsonReaders {
  import play.api.libs.json.Reads._

  implicit val photoReads: Reads[Photo] = (
    (__ \ "source").read[String] ~
    (__ \ "url").read[String]
  )(Photo)

  implicit val pieceOfSheetReads: Reads[PieceOfSheet] = (
    (__ \ "qty").read[Int] ~
    (__ \ "photos").read[List[Photo]]
  )(PieceOfSheet)

  implicit val pieceReads: Reads[Piece] = (
    (__ \ "pieces_type").read[String] ~
    (__ \  "data").read[List[PieceOfSheet]] 
  )((pieceType, sheets) => new Piece(None, pieceType, sheets))
}

object PieceJsonWriters {
  import play.api.libs.json.Writes._
  
  implicit val photoWrites: Writes[Photo] = (
    (__ \ "source").write[String] ~
    (__ \ "url").write[String]
  )(unlift(Photo.unapply))

  implicit val pieceOfSheetWrites: Writes[PieceOfSheet] = (
    (__ \ "qty").write[Int] ~
    (__ \ "photos").write[List[Photo]]
  )(unlift(PieceOfSheet.unapply))

  implicit val pieceWrites: Writes[Piece] = (
  (__ \ "pieces_type").write[String] ~
  (__ \ "data").write[List[PieceOfSheet]]
  )((p: Piece) => (p.pieceType, p.sheets))

}

case class Photo(source: String, url: String)
case class PieceOfSheet(qty: Int, photos: List[Photo])
case class Piece( id: Option[Int], 
                  pieceType: String,
                  sheets: List[PieceOfSheet], 
                  createdAt: Timestamp,
                  token: String) {

  def this(id: Option[Int], pieceType: String, sheets: List[PieceOfSheet]) = 
    this(id, pieceType, sheets, new Timestamp(new java.util.Date().getTime), java.util.UUID.randomUUID.toString)
}

object Pieces extends Table[Piece]("piece") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def pieceType = column[String]("type")
  def jsonData = column[String]("json_data")
  def createdAt = column[Timestamp]("created_at")
  def token = column[String]("token")

  import PieceJsonReaders._
  import PieceJsonWriters._

  def * = id.? ~ pieceType ~ jsonData ~ createdAt ~ token <> (
    (id, pieceType, jsonData, createdAt, token) ⇒
      Piece(id, pieceType, Json.fromJson[List[PieceOfSheet]](Json.parse(jsonData)).get, createdAt, token), 
    
    (p: Piece) ⇒ Some((p.id, p.pieceType, Json.stringify(Json.toJson(p.sheets.map(Json.toJson[PieceOfSheet]))), p.createdAt, p.token))
  )
}