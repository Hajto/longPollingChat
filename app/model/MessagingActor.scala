package model

import akka.actor.Actor
import akka.actor.Actor.Receive
import play.Logger

import scala.concurrent.Promise

case class Subscribe(nick: String, currentChannel: String ,seqId: Long)
case class UnSubsribe(nick: String)
case class BroadcastMessages()
case class Debug()
case class SendMessage(chatMessage: ChatMessage)

class MessagingActor extends Actor{
  case class Member(lastMessage: Long, channel: String , promise: MessagePromise, pw: List[ChatMessage])
  type MessagePromise = Promise[List[ChatMessage]]

  var messages = List[ChatMessage](ChatMessage("","","","",System.currentTimeMillis(),Some(1)))
  var members = Map.empty[String, Member]

  override def receive: Receive = {
    case BroadcastMessages() => {
      refreshMessages()
      members.foreach {
        case (key, member) => {
          val newMessagesForMember = messages
            .filter(msg => msg.id.get > member.lastMessage)
            .filter(msg => msg.channel == member.channel)
          if (newMessagesForMember.size > 0) {
            member.promise.success(newMessagesForMember)
            members -= key
            Logger.info("Broadcasting "+newMessagesForMember.size+" msgs to " + key)
          }
        }
      }
    }
    case SendMessage(chatMessage: ChatMessage) =>
      messages ::= ChatMessage(chatMessage.name,chatMessage.color,chatMessage.channel,chatMessage.chatMessage,chatMessage.currentTime, Some(messages.head.id.get+1))
    case Subscribe(nick: String, currentChanel: String ,lastMessage: Long) => {
      val member =  Member(lastMessage, currentChanel ,Promise[List[ChatMessage]](), List[ChatMessage]())
      members = members + (nick -> member)

      sender ! member.promise
      println(nick+" has subscribed at "+lastMessage)
    }
    case UnSubsribe(nick: String) => members -= nick
    case Debug() => println("Currently " + members.size + " people on Chat " + members.keys)
  }

  def refreshMessages() = {
    messages = messages.filter(chatMessage => isMessageFresh(chatMessage.currentTime))
  }

  def isMessageFresh(timestamp: Long) = {
    timestamp + 5 > System.currentTimeMillis / 1000
  }
}
