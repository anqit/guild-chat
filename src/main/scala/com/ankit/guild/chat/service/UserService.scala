package com.ankit.guild.chat.service

import com.ankit.guild.chat.model.User
import com.ankit.guild.chat.data.dao.UserDao

import scala.concurrent.Future

class UserService(userDao: UserDao) {
  def getOrCreate(user: User): Future[Option[User]] = userDao.getOrCreate(user)
}

object UserService {
  def apply(userDao: UserDao) = new UserService(userDao)
}
