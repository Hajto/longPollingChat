package model

import akka.actor.Actor
import akka.actor.Actor.Receive
import play.Logger

import scala.concurrent.Promise

case class Subscribe(nick: String, seqId: Long)
case class UnSubsribe(nick: String)
case class BroadcastMessages()
case class Debug()
case class SendMessage(chatMessage: ChatMessage)

class MessagingActor extends Actor{
  case class Member(lastMessage: Long, promise: MessagePromise)
  type MessagePromise = Promise[List[ChatMessage]]

  var messages = List[ChatMessage]()
  var members = Map.empty[String, Member]

  override def receive: Receive = {
    case BroadcastMessages() => {
      refreshMessages()
      members.foreach {
        case (key, member) => {
          val newMessagesForMember = messages.filter(msg => msg.currentTime > member.lastMessage)
          if (newMessagesForMember.size > 0) {
            member.promise.success(newMessagesForMember)
            members -= key
            Logger.info("Broadcasting "+newMessagesForMember.size+" msgs to " + key)
          }
        }
      }
    }
    case SendMessage(chatMessage: ChatMessage) =>
      messages ::= chatMessage
    case Subscribe(nick: String, lastMessage: Long) => {
      val member =  Member(lastMessage, Promise[List[ChatMessage]]())
      members = members + (nick -> member)

      sender ! member.promise
      println(nick+" has subscribed at "+lastMessage)
    }
    case UnSubsribe(nick: String) => members -= nick
    case Debug() => println("Currently " + members.size + " people on Chat")
  }

  def refreshMessages() = {
    messages = messages.filter(chatMessage => isMessageFresh(chatMessage.currentTime))
  }

  def isMessageFresh(timestamp: Long) = {
    timestamp + 5 > System.currentTimeMillis / 1000
  }
}
