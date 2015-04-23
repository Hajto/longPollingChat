package model

import akka.actor.Actor
import akka.actor.Actor.Receive

case class SendMessage(chatMessage: ChatMessage)
case class ListenForMessages(clientId: String, seqId: Int)
case class BroadcastMessages()
case class FilterMessages()

class MessagingActor extends Actor{
  case class Member(seqId: Int, promise: MessagesPromise)

  var messages = List[ChatMessage]()
  var members = Map.empty[String, Member]

  override def receive: Receive = {
    case BroadcastMessages() => {
      filterMessages
    }
  }

  def filterMessages = {
    messages = messages.filter(chatMessage => isMessageFresh(chatMessage.currentTime))
  }

  def isMessageFresh(timestamp: Long) = {
    timestamp + 60 > System.currentTimeMillis / 1000
  }
}
