package com.ankit.guild.chat.service

import com.ankit.guild.chat.data.dao.MessageDao
import com.ankit.guild.chat.model.Message

import scala.concurrent.Future

class MessageService(messageDao: MessageDao) {
  def recordMessage(message: Message): Future[Option[Message]] = messageDao.insertMessage(message)

  def getMessagesForRoom(roomId: Int): Future[Seq[Message]] = messageDao.getMessagesForRoom(roomId)
}

object MessageService {
  def apply(messageDao: MessageDao) = new MessageService(messageDao)
}