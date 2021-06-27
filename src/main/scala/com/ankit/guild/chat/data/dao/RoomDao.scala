package com.ankit.guild.chat.data.dao

import com.ankit.guild.chat.model.Room

import scala.concurrent.Future

trait RoomDao {
  def createRoom(room: Room): Future[Option[Room]]
}
