package controllers

import akka.actor.{Props, Actor}
import akka.pattern.ask
import akka.util.Timeout
import model._
import play.api._
import play.api.libs.json.{JsObject, Json, JsError}
import play.api.mvc._
import model.JSONFormats.paperClipFormat
import play.libs.Akka
import scala.concurrent.{TimeoutException, Future, Await, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
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

  implicit val timeout = Timeout(30 second)

  def poll(nick:String, timestamp: Long) = Action{
    val promiseOfResult = waitForList(nick, timestamp)
    try {
      val outCome = Await.result(promiseOfResult.future, 30 second)//.asInstanceOf[List[ChatMessage]]
      Ok(Json.toJson(outCome))
    } catch {
      case e: TimeoutException => {
        messagingActor ! UnSubsribe(nick)
        Ok(Json.toJson(List()))
      }
    }

  }

  def waitForList(nick: String, timestamp: Long): Promise[List[ChatMessage]]  = {
    Await.result(messagingActor.ask(Subscribe(nick, timestamp)),30 second).asInstanceOf[Promise[List[ChatMessage]]]
  }

  lazy val messagingActor = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val actor = Akka.system.actorOf(Props[MessagingActor])
    // Tell the actor to broadcast messages every 1 second
    Akka.system.scheduler.schedule(0 seconds, 200 millis, actor, BroadcastMessages())
    Akka.system.scheduler.schedule(0 seconds, 10 seconds, actor, Debug())

    actor
  }

}