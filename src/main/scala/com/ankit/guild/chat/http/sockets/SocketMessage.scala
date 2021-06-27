package com.ankit.guild.chat.http.sockets

import akka.http.scaladsl.model.ws.TextMessage
import com.ankit.guild.chat.model.Room
import spray.json
import spray.json.{JsArray, JsNumber, JsObject, JsString, JsValue}

sealed trait SocketMessage {
//  def action: String
}



object SocketMessage {
  // incoming
  case class CreateRoom(name: String) extends SocketMessage
  case class SendChatMessage(author: String, roomId: Int, message: String) extends SocketMessage {
    //  val action = "send_message"
  }

  // outgoing
  case class RoomCreated(room: Room) extends SocketMessage
  case class Error(reason: String) extends SocketMessage

  import Actions._
  def getResponse(message: SocketMessage): SocketMessage = {

  }

  def fromJsValue(jsValue: JsValue) = jsValue match {
    case JsArray(elements) => fromList(elements.toList)
  }

  def fromList(params: List[JsValue]): SocketMessage = params match {
    case JsString(cmd) :: (args: JsObject) => fromCmdAndArgsObj(cmd, args)
  }

  def fromCmdAndArgsObj(cmd: String, argsObject: JsObject): SocketMessage = (cmd, argsObject) match {
    case (SendMessageAction, JsObject(args)) => SendChatMessage(args("author").asInstanceOf[json.JsString].value,
      args("roomId").asInstanceOf[JsNumber].value.intValue, args("message").asInstanceOf[JsString].value)
  }

  def toJsValue(message: SocketMessage): JsArray = message match {
    case SendChatMessage(author, roomId, message) =>
      val args = JsObject(("author", JsString(author)), ("roomId", JsNumber(roomId)), ("message", JsString(message)))
      JsArray(JsString(SendMessageAction), args)
  }

  def toWebSocketMessage(messageJson: JsArray): TextMessage = TextMessage(messageJson.toString())
}

case object Actions {
  val SendMessageAction = "send_message"
}