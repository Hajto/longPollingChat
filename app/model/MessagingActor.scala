package model

import akka.actor.Actor
import akka.actor.Actor.Receive
import play.Logger

import scala.concurrent.Promise

case class ListenForMessages(clientId: String, seqId: Int)
case class BroadcastMessages()
case class SendMessage(chatMessage: ChatMessage)

class MessagingActor extends Actor{
  case class Member(seqId: Int, promise: MessagePromise)
  type MessagePromise = Promise[List[ChatMessage]]

  var messages = List[ChatMessage]()
  var members = Map.empty[String, Member]

  override def receive: Receive = {
    case BroadcastMessages() => {
      refreshMessages()

      members.foreach {
        case (key, member) => {
          val newMessagesForMember = messages.filter(msg => msg.currentTime > member.seqId)
          if (newMessagesForMember.size > 0) {
            //member.promise.(newMessagesForMember)
            members -= key
            Logger.info("Broadcasting "+newMessagesForMember.size+" msgs to " + key)
          }
        }
      }
    }
    case SendMessage(chatMessage: ChatMessage) =>
      messages ::= chatMessage
  }

  def refreshMessages() = {
    messages = messages.filter(chatMessage => isMessageFresh(chatMessage.currentTime))
  }

  def isMessageFresh(timestamp: Long) = {
    timestamp + 5 > System.currentTimeMillis / 1000
  }
}
