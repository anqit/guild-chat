package com.ankit.guild.chat.data.dao.slickdao

import com.ankit.guild.chat.data.dao.RoomDao
import com.ankit.guild.chat.data.schema.slickschema.SlickSchema
import com.ankit.guild.chat.model.Room
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

class SlickRoomDao(val schema: SlickSchema, val db: JdbcBackend#Database)(implicit ex: ExecutionContext) extends RoomDao {
  override def createRoom(room: Room): Future[Option[Room]] = db.run(insertRoomAction(room))

  override def getRooms(): Future[Seq[Room]] = db.run(getAllAction())

  import schema.profile.api._
  val rooms = schema.rooms

  private def insertRoomAction(room: Room): DBIO[Option[Room]] =
    ((rooms returning rooms.map(_.id) into ((r, id) => r.copy(id = Some(id)))) += room) map {
      case r: Room => Some(r)
      case _ => None
    }
  private def getAllAction() = rooms.result
}

object SlickRoomDao {
  def apply(schema: SlickSchema, db: JdbcBackend#Database)(implicit ex: ExecutionContext) =
    new SlickRoomDao(schema, db)
}
