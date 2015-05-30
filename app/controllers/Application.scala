package controllers

import akka.actor.{Props, Actor}
import akka.pattern.ask
import akka.util.Timeout
import model._
import play.api._
import play.api.libs.json.{JsObject, Json, JsError}
import play.api.mvc._
import model.JSONFormats.{memberFormat,messageFormat}
import scala.concurrent.ExecutionContext.Implicits.global
import play.libs.Akka
import scala.concurrent.{TimeoutException, Future, Await, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def onMessage() = Action.async { request =>
    request.body.asJson.map { json =>
      json.validate[ChatMessage].map{ chat =>
        messagingActor ! SendMessage(chat)
        Future.successful(Ok("OK"))
      }.recoverTotal{
        e => Future.successful(BadRequest("Detected error:"+ JsError.toFlatJson(e)))
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def onPW(nick:String) = Action {
    Ok(nick)
  }

  implicit val timeout = Timeout(30 second)

  /*def poll = Action{ req =>
    req.body.asJson.map { json =>
      json.validate[Member].map { member =>
        val promiseOfResult = waitForList(member.name, member.channel.get, member.currentTime)
        try {
          val outCome = Await.result(promiseOfResult, 30 second)
          Ok(Json.toJson(outCome))
        } catch {
          case e: TimeoutException =>
            Ok("NOOP")
        }
      }.recoverTotal {e => BadRequest("NOOP")}
    }.getOrElse {
      BadRequest("Expecting Json data")
    }
  }*/

  def poll = Action.async { implicit req =>
    req.body.asJson.map { json =>
      json.validate[Member].map { member =>
        val promiseOfResult = waitForList(member.name, member.channel.get, member.currentTime)
        val timeoutFuture = play.api.libs.concurrent.Promise.timeout("string", 20 seconds)
        Future.firstCompletedOf(Seq(promiseOfResult, timeoutFuture)).map {
          case i: List[ChatMessage] => Ok(Json.toJson(i))
          case t: String => Ok(Json.toJson(List[ChatMessage]()))
        }
      }.recoverTotal {e => Future.successful(BadRequest("NOOP"))}
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def unsub = Action.async { implicit req =>
    req.body.asJson.map { json =>
      json.validate[ChatMessage].map{ chat =>
        messagingActor ! UnSubsribe(chat.name)
        messagingActor ! SendMessage(chat)
        Future.successful(Ok("OK"))
      }.recoverTotal{
        e => Future.successful(BadRequest("Detected error:"+ JsError.toFlatJson(e)))
      }
    }.getOrElse {
      Future.successful(BadRequest("Expecting Json data"))
    }
  }

  def waitForList(nick: String, currentChannel: String ,timestamp: Long): Future[List[ChatMessage]]  = {
    Await.result(messagingActor.ask(Subscribe(nick, currentChannel ,timestamp)),30 second).asInstanceOf[Future[List[ChatMessage]]]
  }

  lazy val messagingActor = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val actor = Akka.system.actorOf(Props[MessagingActor])
    // Tell the actor to broadcast messages every 1 second
    Akka.system.scheduler.schedule(0 seconds, 100 millis, actor, BroadcastMessages())
    Akka.system.scheduler.schedule(0 seconds, 10 seconds, actor, Debug())

    actor
  }
}