package com.ankit.guild.chat.http.sockets

import com.ankit.guild.chat.model.Room
import com.ankit.guild.chat.service.RoomService
import SocketMessage._

import scala.concurrent.{ExecutionContext, Future}

class MessageProcesser(roomService: RoomService)(implicit ex: ExecutionContext) {
  def process(socketMessage: SocketMessage): Future[SocketMessage] = socketMessage match {
    case CreateRoom(name) => roomService.createRoom(Room(name)) map {
      case Some(room) => RoomCreated(room)
      case _ => Error("failed creating room")
    }
  }
}
