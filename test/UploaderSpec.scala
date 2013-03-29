package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import controllers.UploadController

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
}