package model

case class Member(name:String, currentTime: Long, channel: Option[String])
case class ChatMessage(name: String, color: String, channel: String, chatMessage: String, currentTime: Long, id: Option[Long])
object JSONFormats {
  import play.api.libs.json.Json

  implicit val memberFormat = Json.format[Member]
  implicit val messageFormat = Json.format[ChatMessage]
}