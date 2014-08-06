package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.Files
import play.api.libs.Files._
import java.io._
import java.nio.file._
import java.security._
import play.api.libs.json._
import play.api.Play.current

object UploadController extends Controller {
  import scala.concurrent.ExecutionContext.Implicits.global

  def relativePathForHash(hash: String): Option[String] = {
    if (hash.length < 40) 
      None
    else
      Some(hash.grouped(2).take(3).mkString("/") + "/" + hash.substring(6))
  }
  
  def filePathForHash(hash: String): Option[String] = 
    relativePathForHash(hash).map(Play.getFile("uploads").getAbsolutePath + "/" + _)

  def sha1FileParser: BodyParser[(String, File)] = BodyParser((header: RequestHeader) ⇒ {
    val toFile = TemporaryFile(prefix = "chunk")
    Iteratee.fold[Array[Byte], (MessageDigest, FileOutputStream)](MessageDigest.getInstance("SHA-1"), new FileOutputStream(toFile.file)) 
    { 
      case ((md, os), data) ⇒ 
        md.update(data)
        os.write(data)
        (md, os)
    } map { 
      case (md, os) ⇒
        os.close
        val sha1 = md.digest.map("%02x".format(_)).mkString
        Right((sha1, toFile.file))
    }
  })

  def sha1FileParserCheckHash(hash: String): BodyParser[(String, File)] = BodyParser((header: RequestHeader) ⇒ 
    filePathForHash(hash).map(path ⇒ {
      val targetFile = new File(path)
      if (targetFile.exists) 
        Done[Array[Byte], Either[Result, (String, File)]](Left(Ok(Json.toJson(
          Map("sha1" -> Json.toJson(hash), 
              "path" -> Json.toJson(s"/upload/$hash"), 
              "uploaded" -> Json.toJson(true))))), Input.Empty)
      else {
        sha1FileParser(header).map({
          case Right((sha1, file)) ⇒ {
            if (!sha1.equalsIgnoreCase(hash)) {
              Logger.info(s"sha1: $sha1 != hash: $hash")
              file.delete
              Left(PreconditionFailed(Json.toJson(Map("error" -> "sha-1 doesn't match"))))
            } else {
              java.nio.file.Files.createDirectories(targetFile.toPath.getParent)
              java.nio.file.Files.move(file.toPath, targetFile.toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
              Right((sha1, targetFile))
            }
          }
          case result ⇒ result
        })
      }
    }).getOrElse(Done[Array[Byte], Either[Result, (String, File)]](Left(NotFound(Json.toJson(Map("error" -> s"$hash is not valid")))), Input.Empty))
  )

  def uploadToHash(hash: String) = Action(sha1FileParserCheckHash(hash)) { request ⇒
    val (sha1: String, file: File) = request.body
    Ok(Json.toJson(Map("sha1" -> sha1, "path" -> s"/upload/$sha1")))
  }
 
  def getHash(hash: String) = Action { request ⇒
    val pureHash = hash.split('/').mkString.replaceAll("\\.[^.]*$", "")
    val contentType = hash.split('.').last.toLowerCase match {
      case "png" => "image/png"
      case "pdf" => "application/pdf"
      case "jpg" => "image/jpeg"
      case _ => BINARY
    }

    filePathForHash(pureHash).map(path ⇒ {
      val file = new File(path)

      if (file.exists && file.isFile) {
        val fileData = Enumerator.fromFile(file)        

        Result(header = ResponseHeader(OK, Map(CONTENT_LENGTH -> file.length.toString, CONTENT_TYPE -> contentType)), body = fileData)
      } else {
        NotFound
      }

    }).getOrElse(NotFound)
  }

  def upload = Action(sha1FileParser) { request ⇒
    val (sha1: String, file: File) = request.body
    Logger.info("file: " + file)
    filePathForHash(sha1).foreach(path ⇒ {
      java.nio.file.Files.createDirectories(new File(path).toPath.getParent)
      java.nio.file.Files.move(file.toPath, new File(path).toPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING)
    })
    Ok(Json.toJson(Map("sha1" -> sha1, "path" -> s"/upload/$sha1")))
  }
}