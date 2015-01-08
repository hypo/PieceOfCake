package models

import java.io.{InputStream, ByteArrayInputStream, File}

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

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import javax.imageio._
import java.awt.{Image, Color}
import java.awt.image._

case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

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
  import scala.concurrent.ExecutionContext.Implicits.global
  import play.api.Play.current

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

  def inputStreamOf(p: Photo): Future[InputStream] = {
    val bytes: Future[Array[Byte]] =
      WS.url(p.url).getStream().flatMap { response =>
        response._2 |>>> Iteratee.consume[Array[Byte]]()
      }
    bytes.map(new ByteArrayInputStream(_))
  }

  def pdfPages(doc: PDDocument): Seq[Future[PDPage]] = {
    val pagedImageStreams: Seq[Seq[Future[InputStream]]] = photos.grouped(6)
      .map(sixPhotos => sixPhotos.map(inputStreamOf)).toSeq

    val pages = pagedImageStreams.map((inputStreamFutures: Seq[Future[InputStream]]) =>
      Future.sequence(inputStreamFutures).map((inputStreams: Seq[InputStream]) => {
        val pageWidth = 15.24.cm
        val pageHeight = 20.32.cm
        val page = new PDPage(new PDRectangle(pageWidth.toFloat, pageHeight.toFloat))
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
        val rectWidth = 13.0.cm
        val rectHeight = 19.0.cm
        val extraWidth = pageWidth + 2.0.cm
        val extraHeight = pageHeight + 2.0.cm
        contentStream.addRect(((pageWidth - extraWidth) / 2).toFloat, ((pageHeight - rectHeight) / 2).toFloat, extraWidth.toFloat, rectHeight.toFloat)
        contentStream.stroke()
        contentStream.addRect(((pageWidth - rectWidth) / 2).toFloat, ((pageHeight - extraHeight) / 2).toFloat, rectWidth.toFloat, extraHeight.toFloat)
        contentStream.stroke()
        contentStream.close()
        page
      })
    )
    List.fill(qty)(pages).flatten
  }

  def thumbnail(path: String, sideLength: Int = 60): Future[RenderedImage] = {
    Future.sequence(photos.map(p => inputStreamOf(p).map(ImageIO.read))).map((images: Seq[BufferedImage]) => {
      val canvas = new BufferedImage(sideLength * 3, sideLength * 4, BufferedImage.TYPE_INT_RGB)
      val g = canvas.getGraphics

      images.take(12).zipWithIndex.foreach { case (img, idx) =>
        Logger.info(s"${img.getWidth} x ${img.getHeight}")
        val rescaled = img.getScaledInstance(sideLength, sideLength, Image.SCALE_AREA_AVERAGING)
        g.drawImage(rescaled, sideLength * (idx % 3), sideLength * (idx / 3), sideLength, sideLength, null)
      }

      ImageIO.write(canvas, "png", new File(path))
      canvas
    })
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
  import play.api.db.slick.DB
  import play.api.Play.current

  val Pieces = TableQuery[Pieces]

  def list(page: Int = 0, pageSize: Int = 20): Page[Piece] = DB.withTransaction { implicit  session => {
    val offset = pageSize * page
    val items = Pieces.sortBy(_.createdAt.desc).drop(offset).take(pageSize).list
    val totalRows: Int = Pieces.length.run
    Page(items, page = page, offset = offset, totalRows)
  }}
}