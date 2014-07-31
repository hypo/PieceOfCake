package models

import scala.slick.driver.PostgresDriver.simple._
import play.api.Play.current

import java.sql._

import play.api.libs.json._
import play.api.libs.functional.syntax._

object PieceJsonReaders {
  import play.api.libs.json.Reads._

  implicit val photoReads: Reads[Photo] = (
    (__ \ "source").read[String] and
    (__ \ "url").read[String]
  )(Photo)

  implicit val pieceOfSheetReads: Reads[PieceOfSheet] = (
    (__ \ "qty").read[Int] and
    (__ \ "photos").read[List[Photo]]
  )(PieceOfSheet)

  implicit val pieceReads: Reads[Piece] = (
    (__ \ "pieces_type").read[String] and
    (__ \  "data").read[List[PieceOfSheet]] 
  )((pieceType, sheets) => new Piece(None, pieceType, sheets))
}

object PieceJsonWriters {
  import play.api.libs.json.Writes._
  
  implicit val photoWrites: Writes[Photo] = (
    (__ \ "source").write[String] and
    (__ \ "url").write[String]
  )(unlift(Photo.unapply))

  implicit val pieceOfSheetWrites: Writes[PieceOfSheet] = (
    (__ \ "qty").write[Int] and
    (__ \ "photos").write[List[Photo]]
  )(unlift(PieceOfSheet.unapply))

  implicit val pieceWrites: Writes[Piece] = (
  (__ \ "pieces_type").write[String] and
  (__ \ "data").write[List[PieceOfSheet]]
  )((p: Piece) => (p.pieceType, p.sheets))

}

case class Photo(source: String, url: String)
case class PieceOfSheet(qty: Int, photos: List[Photo]) {
  def pcdString: String = 
s"""
beginpdf 340.15748 510.23622 # 12cm x 18cm
${
  photos.zipWithIndex.take(6).map({ case (p, idx) => 
    s"simpleimage ${p.url} ${(idx % 2) * 6.0 * 28.3464567} ${(2 - idx / 2) *  6.0 * 28.3464567} ${6.0 * 28.3464567} ${6.0 * 28.3464567}"
  }).mkString("\n")
}
endpdf file:///tmp/piece.pdf
"""  
}

case class Piece( id: Option[Int], 
                  pieceType: String,
                  sheets: List[PieceOfSheet], 
                  createdAt: Timestamp,
                  token: String) {

  def this(id: Option[Int], pieceType: String, sheets: List[PieceOfSheet]) = 
    this(id, pieceType, sheets, new Timestamp(new java.util.Date().getTime), java.util.UUID.randomUUID.toString)
}

class Pieces(tag: Tag) extends Table[Piece](tag, "piece") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def pieceType = column[String]("type")
  def jsonData = column[String]("json_data")
  def createdAt = column[Timestamp]("created_at")
  def token = column[String]("token")

  import PieceJsonReaders._
  import PieceJsonWriters._

  def * = (id?, pieceType, jsonData, createdAt, token).shaped <> (
    (tuple: (Option[Int], String, String, Timestamp, String)) ⇒ { 
        val (id, pieceType, jsonData, createdAt, token) = tuple
        Piece(id, pieceType, Json.fromJson[List[PieceOfSheet]](Json.parse(jsonData)).get, createdAt, token)
    },
    (p: Piece) ⇒ Some((p.id, p.pieceType, Json.stringify(Json.toJson(p.sheets.map(Json.toJson[PieceOfSheet]))), p.createdAt, p.token))
  )
}

object PiecesDAO {
  val Pieces = TableQuery[Pieces]
}