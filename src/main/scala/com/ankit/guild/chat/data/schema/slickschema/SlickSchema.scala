package com.ankit.guild.chat.data.schema.slickschema

import com.ankit.guild.chat.model.{Message, Room, /* RoomMembership,*/ User}
import slick.jdbc.JdbcProfile

import java.time.Instant

class SlickSchema(val profile: JdbcProfile) extends SlickProfileProvider {
  import profile.api._

  // Users
  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("user_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("user_name", O.Unique)

    def * = (name, id.?).mapTo[User]
  }

  lazy val users = TableQuery[Users]

  // Rooms
  case class LiftedRoom(name: Rep[String], id: Rep[Option[Int]])
  implicit object RoomShape extends CaseClassShape(LiftedRoom.tupled, (Room.makeEmpty _).tupled)

  class Rooms(tag: Tag) extends Table[Room](tag, "rooms") {
    def id = column[Int]("room_id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("room_name")

    def * = LiftedRoom(name, id.?)
  }

  lazy val rooms = TableQuery[Rooms]

  // Room members
//  class RoomMembers(tag: Tag) extends Table[RoomMembership](tag, "room_members") {
//    def roomId = column[Int]("room_id")
//    def userName = column[String]("user")
//
//    def pk = primaryKey("room_members_pk", (roomId, userName))
//
//    def room = foreignKey("membership_room_fk", roomId, rooms)(_.id)
//    def user = foreignKey("membership_user_fk", userName, users)(_.name)
//
//    def * = (roomId, userName).mapTo[RoomMembership]
//  }
//
//  lazy val roomMembers = TableQuery[RoomMembers]

  class Messages(tag: Tag) extends Table[Message](tag, "messages") {
    def roomId = column[Int]("room_id")
    def authorName = column[String]("author")
    def message = column[String]("message")
    def timestamp = column[Instant]("timestamp")

    def room = foreignKey("message_room_fk", roomId, rooms)(_.id)
    def author = foreignKey("message_user_fk", authorName, users)(_.name)

    def * = (roomId, authorName, message, timestamp.?).mapTo[Message]
  }

  lazy val messages = TableQuery[Messages]
}

object SlickSchema {
  def apply(profile: JdbcProfile) = new SlickSchema(profile)
}
