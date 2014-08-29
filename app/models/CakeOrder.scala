package models

import java.io.{InputStream, ByteArrayInputStream}

import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg
import org.apache.pdfbox.pdmodel.{PDDocument, PDPage}
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.ws.{WSResponseHeaders, WS}

import scala.concurrent.Future
import scala.slick.driver.PostgresDriver.simple._
import scala.language.postfixOps
import java.sql.Timestamp

import play.api.libs.json._
import play.api.libs.functional.syntax._

object PieceJsonReaders {
  import play.api.libs.json.Reads._

  implicit val photoReads = Json.reads[Photo]
  implicit val pieceOfSheetReads = Json.reads[PieceOfSheet]

  implicit val pieceReads: Reads[Piece] = (
    (__ \ "pieces_type").read[String] and
    (__ \  "data").read[List[PieceOfSheet]] 
  )((pieceType, sheets) => new Piece(None, pieceType, sheets))
}

object PieceJsonWriters {
  import play.api.libs.json.Writes._
  
  implicit val photoWrites = Json.writes[Photo]
  implicit val pieceOfSheetWrites = Json.writes[PieceOfSheet]

  implicit val pieceWrites: Writes[Piece] = (
  (__ \ "pieces_type").write[String] and
  (__ \ "data").write[List[PieceOfSheet]]
  )((p: Piece) => (p.pieceType, p.sheets))
}


object helper {
  implicit class PostscriptPointDouble(val pt: Double) extends AnyVal {
    def cm = pt * 28.3464567
    def inch = pt * 72
  }
}

case class Photo(source: String, url: String)
case class PieceOfSheet(qty: Int, photos: List[Photo]) {
  import helper._

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
  def pdfPages(doc: PDDocument): Seq[Future[PDPage]] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    import play.api.Play.current

    def inputStreamOf(p: Photo): Future[InputStream] = {
      val bytes: Future[Array[Byte]] =
        WS.url(p.url).getStream().flatMap { response =>
          response._2 |>>> Iteratee.consume[Array[Byte]]()
        }
      bytes.map(new ByteArrayInputStream(_))
    }

    val pagedImageStreams: Seq[Seq[Future[InputStream]]] = photos.grouped(6)
      .map(sixPhotos => sixPhotos.map(inputStreamOf)).toSeq

    val pages = pagedImageStreams.map((inputStreamFutures: Seq[Future[InputStream]]) =>
      Future.sequence(inputStreamFutures).map((inputStreams: Seq[InputStream]) => {
        val page = new PDPage(new PDRectangle(15.24.cm.toFloat, 20.32.cm.toFloat))
        val images = inputStreams.map(is => new PDJpeg(doc, is))
        val contentStream = new PDPageContentStream(doc, page, true, true)
        val origin_x = (15.24.cm - 12.0.cm) / 2
        val origin_y = (20.32.cm - 18.0.cm) / 2
        val w = 6.0.cm
        val h = 6.0.cm
        images.zipWithIndex.foreach { case (img, idx) =>
            contentStream.drawXObject(img,
              (origin_x + (idx % 2) * w).toFloat,
              (origin_y + Math.floor(idx / 2) * h).toFloat,
              w.toFloat, h.toFloat)
        }
        contentStream.close()
        page
      })
    )
    List.fill(qty)(pages).flatten
  }
}

case class Piece( id: Option[Int], 
                  pieceType: String,
                  sheets: List[PieceOfSheet], 
                  createdAt: Timestamp,
                  token: String) {

  def this(id: Option[Int], pieceType: String, sheets: List[PieceOfSheet]) = 
    this(id, pieceType, sheets, new Timestamp(new java.util.Date().getTime), java.util.UUID.randomUUID.toString)

  def pdfDocument(path: String): Future[PDDocument] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val doc = new PDDocument()
    Future.sequence(sheets.flatMap(_.pdfPages(doc))).map(pages => {
      pages.foreach(page => doc.addPage(page))
      doc.save(path)
      doc.close()
      doc
    })
  }
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