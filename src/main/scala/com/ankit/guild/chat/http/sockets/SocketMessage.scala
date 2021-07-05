package com.ankit.guild.chat.http.sockets

import akka.http.scaladsl.model.ws.TextMessage
import com.ankit.guild.chat.http.json.JsonSupport._
import com.ankit.guild.chat.model.{Message, Room}
import spray.json.{JsArray, JsObject, JsString, JsValue, JsonFormat, enrichAny}

sealed trait SocketMessage

object SocketMessage {
  // incoming
  case class CreateRoom(room: Room) extends SocketMessage
  case class GetRooms() extends SocketMessage
//  case class JoinRoom(roomId: Int, user: String) extends SocketMessage
//  case class LeaveRoom()
  case class SendChatMessage(msg: Message) extends SocketMessage


  // outgoing
  case class RoomCreated(room: Room) extends SocketMessage
  case class RoomsRetrieved(rooms: Seq[Room]) extends SocketMessage
  case class MessageSent(message: Message) extends SocketMessage
  case class Error(reason: String) extends SocketMessage

  import Actions._

  def fromJsValue(jsValue: JsValue) = jsValue match {
    case JsArray(elements) => fromList(elements.toList)
    case _ => Error("expected json array")
  }

  def fromList(params: List[JsValue]): SocketMessage = params match {
    case JsString(cmd) :: (args: JsObject) :: Nil => fromCmdAndArgsObj(cmd, args)
    case _ => Error("expected array of command name and parameter object")
  }

  def fromCmdAndArgsObj(cmd: String, params: JsObject): SocketMessage = cmd match {
    case CreateRoomAction => CreateRoom(params.convertTo[Room])
    case GetRoomsAction => GetRooms()
    case SendMessageAction => SendChatMessage(params.convertTo[Message])
    case RoomCreatedAction => RoomCreated(params.convertTo[Room])
    case MessageSentAction => MessageSent(params.convertTo[Message])
    case ErrorAction => Error(params.getFields("reason").head.asInstanceOf[JsString].value)
    case u => Error(s"unrecognized message type $u")
  }

  def toJsValue(message: SocketMessage): JsArray = message match {
    case CreateRoom(room) =>
      toJsValue(CreateRoomAction, room)
    case GetRooms() =>
      JsArray(JsString(GetRoomsAction))
    case SendChatMessage(message) =>
      toJsValue(SendMessageAction, message)
    case RoomCreated(room) =>
      toJsValue(RoomCreatedAction, room)
    case RoomsRetrieved(rooms) =>
      toJsValue(RoomsRetrievedAction, rooms)
    case MessageSent(message) =>
      toJsValue(MessageSentAction, message)
    case Error(reason) =>
      JsArray(JsString(ErrorAction), JsObject("reason" -> JsString(reason)))
  }

  def toJsValue[A: JsonFormat](action: String, param: A) = JsArray(JsString(action), param.toJson)

  def toWebSocketMessage(messageJson: JsArray): TextMessage = TextMessage(messageJson.toString())

  def isGlobalResponse(socketMessage: SocketMessage): Boolean = socketMessage match {
    case CreateRoom(_) => true
    case GetRooms() => false
    //  case JoinRoom(_, _) => false
    //  case LeaveRoom() => false
    case SendChatMessage(_) => true

    // outgoing
    case RoomCreated(_) => true
    case RoomsRetrieved(_) => false
    case MessageSent(_) => true
    case Error(_) => true
  }
}

case object Actions {
  val CreateRoomAction = "create_room"
  val GetRoomsAction = "get_rooms"
  val SendMessageAction = "send_message"

  val RoomCreatedAction = "room_created"
  val RoomsRetrievedAction = "rooms_retrieved"
  val MessageSentAction = "message_sent"
  val ErrorAction = "error"
}
