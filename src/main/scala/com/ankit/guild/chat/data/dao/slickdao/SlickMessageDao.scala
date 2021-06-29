package com.ankit.guild.chat.data.dao.slickdao

import com.ankit.guild.chat.data.dao.MessageDao
import com.ankit.guild.chat.data.schema.slickschema.SlickSchema
import com.ankit.guild.chat.model.Message
import slick.jdbc.JdbcBackend

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

class SlickMessageDao(val schema: SlickSchema, val db: JdbcBackend#Database)(implicit ex: ExecutionContext) extends MessageDao {
  override def getMessagesForRoom(roomId: Int): Future[Seq[Message]] = db.run(getMessagesForRoomAction(roomId, Some(10)))
  override def insertMessage(message: Message): Future[Option[Message]] = db.run(insertAction(message))

  import schema.profile.api._
  val messages = schema.messages
  private def insertAction(message: Message) = ((messages returning messages) += message.copy(timestamp = Some(Instant.now()))) map {
    case m: Message => Some(m)
    case _ => None
  }

  def getMessagesForRoomAction(roomId: Int, limit: Option[Int] = None): DBIO[Seq[Message]] = {
    val q = messages.filter(_.roomId === roomId).sortBy(_.timestamp.desc)

    limit match {
      case Some(l) =>q.take(l).result
      case _ => q.result
    }
  }
}

object SlickMessageDao {
  def apply(schema: SlickSchema, db: JdbcBackend#Database)(implicit ex: ExecutionContext) =
    new SlickMessageDao(schema, db)
}
