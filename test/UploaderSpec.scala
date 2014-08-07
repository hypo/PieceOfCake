package test

import java.io.File

import org.specs2.mutable._
import play.api.http.HeaderNames
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsValue, JsUndefined, JsString}
import play.api.mvc.Result

import play.api.test._
import play.api.test.Helpers._
import controllers.UploadController

import scala.concurrent.Future
import scala.io.Source

class UploaderSpec extends Specification {
  
  "relativePathForHash method" should {
    
    "return Some(relativePath) if given a 40-bytes valid hash" in {
      UploadController.relativePathForHash("87e5b90cd017c8d9816173b445dc71991772281c") must 
        beSome.which(_ == "87/e5/b9/0cd017c8d9816173b445dc71991772281c")
    }

    "return none if the hash is not valid" in {
      UploadController.relativePathForHash("87e5b90cd017c8d9816173b445dc7199177228") must beNone
    }
  }

  "Upload file" should {
    "create a new file identical with the uploaded one with respect to its hash" in {
      running(FakeApplication()) {

        val bytes: Array[Byte] = Array.fill(1024)(0.toByte)
        val expectedHash = "60cacbf3d72e1e7834203da608037b1bf83b40e8"
        val filePath: Option[String] = UploadController.filePathForHash(expectedHash)

        // make sure the file is not here.
        filePath.map(new File(_)).foreach(f => f.delete())

        val action = UploadController.upload
        val req = FakeRequest("POST", "/upload")
          .withHeaders(HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withRawBody(bytes)
        val result: Future[Result] = call(action, req)

        status(result) mustEqual OK

        val file = new File(filePath.get)
        file.exists must beTrue
        file.length() mustEqual 1024
        Source.fromFile(file).map(_.toByte).toVector mustEqual bytes.toVector
      }
    }

    "returns sha1 and path" in {
      running(FakeApplication()) {
        val bytes: Array[Byte] = Array.fill(1024)(0.toByte)
        val expectedHash = "60cacbf3d72e1e7834203da608037b1bf83b40e8"
        val filePath: Option[String] = UploadController.filePathForHash(expectedHash)

        // make sure the file is not here.
        filePath.map(new File(_)).foreach(f => f.delete())

        val action = UploadController.upload
        val req = FakeRequest("POST", "/upload")
          .withHeaders(HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withRawBody(bytes)
        val result: Future[Result] = call(action, req)

        status(result) mustEqual OK
        (contentAsJson(result) \ "sha1").asOpt[String] mustEqual Some(expectedHash)
        contentAsJson(result) \ "path" mustNotEqual None
      }
    }

    "overwrite the existing file with same hash" in {
      running(FakeApplication()) {

        val bytes: Array[Byte] = Array.fill(1024)(0.toByte)
        val expectedHash = "60cacbf3d72e1e7834203da608037b1bf83b40e8"
        val filePath: Option[String] = UploadController.filePathForHash(expectedHash)

        // make sure the file is not here.
        filePath.map(new File(_)).foreach(f => {
          f.delete()
          f.createNewFile()
        })

        val action = UploadController.upload
        val req = FakeRequest("POST", "/upload")
          .withHeaders(HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withRawBody(bytes)
        val result: Future[Result] = call(action, req)

        status(result) mustEqual OK
        (contentAsJson(result) \ "sha1").asOpt[String] mustEqual Some(expectedHash)
        contentAsJson(result) \ "path" mustNotEqual None

        val file = new File(filePath.get)
        file.exists must beTrue
        file.length() mustEqual 1024
        Source.fromFile(file).map(_.toByte).toVector mustEqual bytes.toVector
      }
    }
  }

  "Upload to hash" should {
    "act as upload (without hash provided)" in {
      running(FakeApplication()) {

        val bytes: Array[Byte] = Array.fill(1024)(0.toByte)
        val expectedHash = "60cacbf3d72e1e7834203da608037b1bf83b40e8"
        val filePath: Option[String] = UploadController.filePathForHash(expectedHash)

        // make sure the file is not here.
        filePath.map(new File(_)).foreach(f => f.delete())

        val req = FakeRequest("POST", "/upload/" + expectedHash)
          .withHeaders(HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withRawBody(bytes)
        val resultOpt: Option[Future[Result]] = route(req)
        resultOpt mustNotEqual None
        val result = resultOpt.get

        status(result) mustEqual OK
        (contentAsJson(result) \ "sha1").asOpt[String] mustEqual Some(expectedHash)
        contentAsJson(result) \ "path" mustNotEqual None

        val file = new File(filePath.get)
        file.exists must beTrue
        file.length() mustEqual 1024
        Source.fromFile(file).map(_.toByte).toVector mustEqual bytes.toVector
      }
    }

    "return 404 (not found) if hash is shorter than 40 bytes" in {
      running(FakeApplication()) {

        val bytes: Array[Byte] = Array.fill(1024)(0.toByte)
        val notExpectedHash = "too-short-to-be-a-hash"
        val filePath: Option[String] = UploadController.filePathForHash(notExpectedHash)

        // make sure the file is not here.
        filePath.map(new File(_)).foreach(f => {
          f.delete()
        })

        val req = FakeRequest("POST", "/upload/" + notExpectedHash)
          .withHeaders(HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withRawBody(bytes)
        val resultOpt: Option[Future[Result]] = route(req)
        resultOpt mustNotEqual None
        val result = resultOpt.get
        status(result) mustEqual NOT_FOUND
        (contentAsJson(result) \ "error").asOpt[String] mustNotEqual None
      }
    }

    "return 412 (precondition failed) if hash doesn't match" in {
      running(FakeApplication()) {

        val bytes: Array[Byte] = Array.fill(1024)(0.toByte)
        val notExpectedHash = "cafebabe0000deadbeef1111facefeed2222food"
        val filePath: Option[String] = UploadController.filePathForHash(notExpectedHash)

        // make sure the file is not here.
        filePath.map(new File(_)).foreach(f => {
          f.delete()
        })

        val req = FakeRequest("POST", "/upload/" + notExpectedHash)
          .withHeaders(HeaderNames.CONTENT_TYPE -> "application/x-www-form-urlencoded")
          .withRawBody(bytes)
        val resultOpt: Option[Future[Result]] = route(req)
        resultOpt mustNotEqual None
        val result = resultOpt.get
        status(result) mustEqual PRECONDITION_FAILED
        (contentAsJson(result) \ "error").asOpt[String] mustNotEqual None
      }
    }
  }
}