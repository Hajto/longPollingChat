package controllers

import akka.actor.{Props, Actor}
import model._
import play.api._
import play.api.libs.json.{Json, JsError}
import play.api.mvc._
import model.JSONFormats.paperClipFormat
import play.libs.Akka
import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def onMessage() = Action { request =>
    request.body.asJson.map { json =>
      json.validate[ChatMessage].map{ chat =>
        messagingActor ! SendMessage(chat)
        Ok("OK")
      }.recoverTotal{
        e => BadRequest("Detected error:"+ JsError.toFlatJson(e))
      }
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }

  def poll(timestamp: Int) = Action{
    Ok("")
  }

  lazy val messagingActor = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val actor = Akka.system.actorOf(Props[MessagingActor])
    // Tell the actor to broadcast messages every 1 second
    Akka.system.scheduler.schedule(0 seconds, 1 seconds, actor, BroadcastMessages())

    actor
  }

}