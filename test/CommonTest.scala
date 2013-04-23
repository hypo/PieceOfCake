package test

import play.api.test._

trait RequestMaker {

  def localRequest(method: String, uri: String, body: String, headers: Seq[(String, Seq[String])] = List()) = 
    new FakeRequest(method = method, uri = uri, headers = new FakeHeaders(headers), body = body, remoteAddress = "127.0.0.1")

  def localJsonRequest(method: String, uri: String, body: String, headers: Seq[(String, Seq[String])] = List()) = 
    new FakeRequest(method = method, uri = uri, headers = new FakeHeaders(("Content-Type" -> List("application/json")) +: headers), body = body, remoteAddress = "127.0.0.1")
}