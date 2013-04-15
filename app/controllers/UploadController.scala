package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.Files
import play.api.libs.Files._
import java.io._
import java.security._

 import play.api.Play.current

object UploadController extends Controller {

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
    } mapDone { 
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
        Done[Array[Byte], Either[Result, (String, File)]](Left(Ok("Already uploaded")), Input.Empty)
      else {
        sha1FileParser(header).map({
          case Right((sha1, file)) ⇒ {
            if (sha1.equalsIgnoreCase(hash)) {
              file.delete
              Left(PreconditionFailed("sha-1 doesn't match"))
            } else {
              Files.moveFile(file, targetFile)
              Right((sha1, targetFile))
            }
          }
          case result ⇒ result
        })
      }
    }).getOrElse(Done[Array[Byte], Either[Result, (String, File)]](Left(NotFound(s"$hash is not valid")), Input.Empty))
  )

  def uploadToHash(hash: String) = Action(sha1FileParserCheckHash(hash)) { request ⇒
    val (sha1: String, file: File) = request.body
    Ok(s"""{"sha1":"$sha1", "path":"/uploads/$sha1"}""")
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

        SimpleResult(header = ResponseHeader(OK, Map(CONTENT_LENGTH -> file.length.toString, CONTENT_TYPE -> contentType)), fileData)
      } else {
        NotFound
      }

    }).getOrElse(NotFound)
  }

  def upload = Action(sha1FileParser) { request ⇒
    val (sha1: String, file: File) = request.body
    Logger.info("file: " + file)
    filePathForHash(sha1).foreach(path ⇒ {
      Files.moveFile(file, new File(path))
    })
    Ok(s"""{"sha1":"$sha1", "path":"/upload/$sha1"}""")
  }
}