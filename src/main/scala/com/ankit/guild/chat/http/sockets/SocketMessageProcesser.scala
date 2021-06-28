package com.ankit.guild.chat.http.sockets

import com.ankit.guild.chat.http.sockets.SocketMessage._
import com.ankit.guild.chat.service.{MessageService, RoomService}

import scala.concurrent.{ExecutionContext, Future}

class SocketMessageProcesser(roomService: RoomService, messageService: MessageService)(implicit ex: ExecutionContext) {
  def process(socketMessage: SocketMessage): Future[SocketMessage] = (socketMessage match {
    case CreateRoom(room) => roomService.createRoom(room).map {
        case Some(r) => RoomCreated(r)
        case _ => Error("failed creating room")
      }
    case GetRooms() => roomService.getRooms() map { RoomsRetrieved }
    case SendChatMessage(message) => messageService.recordMessage(message) map {
        case Some(m) => MessageSent(m)
        case _ => Error("failed sending message")
      }
  }) recover {
    case t => Error(t.getMessage)
  }
}

object SocketMessageProcesser {
  def apply(roomService: RoomService, messageService: MessageService)(implicit ex: ExecutionContext) =
    new SocketMessageProcesser(roomService, messageService)
}