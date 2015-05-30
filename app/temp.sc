import model.ChatMessage

var mapa = Map.empty[String,List[ChatMessage]]

mapa += ("key" -> List(ChatMessage("123","123","123","123",1, Some(1))))
val newMess = ChatMessage("223","223","223","223",1, Some(1))
mapa.get("key") match {
  case Some(value) =>
    println("Printing some stuff !")
    println(value)
    mapa += ("key" -> value)
  case None =>
}