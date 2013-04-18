package controllers

import play.api._
import play.api.mvc._

object OrderController extends Controller {
  
  def makeOrder = Action {
    Ok("")
  }
  
  def showOrder(orderNumber: String) = Action {
    Ok("")
  }
}