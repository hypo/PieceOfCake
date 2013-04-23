package controllers

import models._

import play.api._
import play.api.mvc._

object OrderController extends Controller {
  
  def makeOrder = Action(parse.tolerantText(maxLength = 2 * 1024 * 1024)) { request =>
    Logger.info("receive " + request.body)
    Ok("")
  }
  
  def showOrder(orderNumber: String) = Action {
    Ok("")
  }
}