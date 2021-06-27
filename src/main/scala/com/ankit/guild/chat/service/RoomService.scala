package com.ankit.guild.chat.service

import com.ankit.guild.chat.data.dao.RoomDao
import com.ankit.guild.chat.model.Room

import scala.concurrent.Future

class RoomService(roomDao: RoomDao) {
  def createRoom(room: Room): Future[Option[Room]] = roomDao.createRoom(room)
}
