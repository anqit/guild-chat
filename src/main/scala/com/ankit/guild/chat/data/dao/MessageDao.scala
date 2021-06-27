package com.ankit.guild.chat.data.dao

import com.ankit.guild.chat.model.Message

import scala.concurrent.Future

trait MessageDao {
  def getMessagesForRoom(roomId: Int): Future[Seq[Message]]
  def insertMessage(message: Message): Future[Option[Message]]
}
