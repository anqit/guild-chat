package com.ankit.guild.chat.data.dao

import com.ankit.guild.chat.model.User

import scala.concurrent.Future

trait UserDao {
  def getOrCreate(user: User): Future[Option[User]]
}
