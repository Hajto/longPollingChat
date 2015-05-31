package model

import akka.actor.Actor
import akka.actor.Actor.Receive
import play.Logger

import scala.concurrent.Promise

case class Subscribe(nick: String, currentChannel: String ,seqId: Long)
case class UnSubsribe(nick: String)
case class BroadcastMessages()
case class SendMessage(chatMessage: ChatMessage,isPrivate: Boolean)
case class ListUsers()
case class Debug()


class MessagingActor extends Actor{
  case class Member(lastMessage: Long, channel: String, promise: MessagePromise)
  type MessagePromise = Promise[List[ChatMessage]]

  var messages = List[ChatMessage]()
  var privateMailbox = List[ChatMessage]()
  var members = Map.empty[String, Member]
  var lastMessageId = 0

  override def receive: Receive = {
    case BroadcastMessages() => {
      members.foreach {
        case (key, member) => {
          val newMessagesForMember = messages
            .filter(msg => msg.id.get > member.lastMessage)
            .filter(msg => msg.channel == member.channel)
          val privateMessages = privateMailbox
            .filter(msg => msg.name == key)
          if (newMessagesForMember.nonEmpty || privateMessages.nonEmpty) {
            member.promise.success(newMessagesForMember ::: privateMessages)
            members -= key
            privateMailbox = privateMailbox.filter(msg => !(msg.name == key))
            Logger.info("Broadcasting "+newMessagesForMember.size+" msgs to " + key)
          }
        }
      }
    }
    case SendMessage(chatMessage: ChatMessage, isPrivate: Boolean) =>
      if(isPrivate){
        privateMailbox ::= chatMessage
      } else {
        messages ::= ChatMessage(chatMessage.name,chatMessage.color,chatMessage.channel,chatMessage.chatMessage,chatMessage.currentTime, Some(getNextMessageId))
      }
    case Subscribe(nick: String, currentChanel: String ,lastMessage: Long) => {
      val member =  Member(lastMessage, currentChanel ,Promise[List[ChatMessage]]())
      members = members + (nick -> member)

      sender ! member.promise
      println(nick+" has subscribed at "+lastMessage)
    }
    case UnSubsribe(nick: String) => members -= nick
    case ListUsers => sender ! members.keys.toList
    case Debug() =>
      refreshMessages()
      println("Currently " + members.size + " people on Chat " + members.keys + " wiadomosci " + messages.length)
  }

  def refreshMessages() = {
    messages = messages.filter(chatMessage => isMessageFresh(chatMessage.currentTime))
  }

  def getNextMessageId = {
    lastMessageId += 1
    lastMessageId
  }

  def isMessageFresh(timestamp: Long) = {
    timestamp + 500 > System.currentTimeMillis
  }
}
