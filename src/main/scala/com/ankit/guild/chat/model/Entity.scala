package com.ankit.guild.chat.model

import java.time.Instant

case class User(name: String, id: Option[Int] = None) /* extends Entity[User, String]*/

case class Room(name: String, messages: Seq[Message] = List(), id: Option[Int] = None) /* extends Entity[Room, Int] */

object Room {
  def makeEmpty(name: String, id: Option[Int] = None) = Room(name, List(), id)
}
//case class RoomMembership(roomId: Int, user: String)

case class Message(roomId: Int, author: String, message: String, timestamp: Option[Instant] = None)
