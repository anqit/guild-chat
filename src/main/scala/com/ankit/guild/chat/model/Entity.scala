package com.ankit.guild.chat.model

import java.time.Instant

case class User(name: String, id: Option[Int] = None) /* extends Entity[User, String]*/

case class Room(name: String, id: Option[Int] = None) /* extends Entity[Room, Int] */

case class RoomMembership(roomId: Int, user: String)

case class Message(roomId: Int, author: String, message: String, timestamp: Option[Instant] = None)
