package model

case class ChatMessage(name: String, chatMessage: String, currentTime: Long)
object JSONFormats {
  import play.api.libs.json.Json

  implicit val paperClipFormat = Json.format[ChatMessage]
}