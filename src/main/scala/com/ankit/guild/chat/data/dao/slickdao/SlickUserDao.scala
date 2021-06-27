package com.ankit.guild.chat.data.dao.slickdao

import com.ankit.guild.chat.model.User
import com.ankit.guild.chat.data.dao.UserDao
import com.ankit.guild.chat.data.schema.slickschema.SlickSchema
import slick.jdbc.JdbcBackend

import scala.concurrent.Future

class SlickUserDao(val schema: SlickSchema, val db: JdbcBackend#Database) extends UserDao {
  override def getOrCreate(user: User): Future[Option[User]] = db.run(insertOrUpdateAction(user))

  import schema.profile.api._
  val users = schema.users
  private def insertOrUpdateAction(user: User): DBIO[Option[User]] = (users returning users).insertOrUpdate(user)
}

object SlickUserDao {
  def apply(schema: SlickSchema, db: JdbcBackend#Database) =
    new SlickUserDao(schema, db)
}
