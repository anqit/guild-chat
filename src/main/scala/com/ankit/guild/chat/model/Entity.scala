package com.ankit.guild.chat.model

import java.time.Instant

//sealed trait Entity[T <: Entity[T, ID], ID] {
//  def id: Option[ID]
//  def withId(id: ID): T
//}

case class User(name: String) /* extends Entity[User, String]*/

case class Room(name: String, id: Option[Int] = None) /* extends Entity[Room, Int] */

case class RoomMembership(roomId: Int, user: String)

case class Message(roomId: Int, author: String, message: String, timestamp: Instant)
