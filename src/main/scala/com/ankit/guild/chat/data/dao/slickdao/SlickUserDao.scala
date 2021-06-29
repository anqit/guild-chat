package com.ankit.guild.chat.data.dao.slickdao

import com.ankit.guild.chat.model.User
import com.ankit.guild.chat.data.dao.UserDao
import com.ankit.guild.chat.data.schema.slickschema.SlickSchema
import slick.jdbc.JdbcBackend

import scala.concurrent.{ExecutionContext, Future}

class SlickUserDao(val schema: SlickSchema, val db: JdbcBackend#Database)(implicit ex: ExecutionContext) extends UserDao {
  override def getOrCreate(user: User): Future[Option[User]] = {

    db.run(findAction(user)) flatMap {
      case Some(u) => Future.successful(Some(u))
      case None => db.run(createAction(user))
    }
  }

  import schema.profile.api._
  val users = schema.users

  private def findAction(user: User) = user match {
    case User(name, id) => users
      .filterOpt(id)((u, i) => u.id === i)
      .filter(_.name === name)
      .result.headOption
  }

  private def createAction(user: User) =
    ((users returning users.map(_.id) into ((u, i) => u.copy(id = Some(i)))) += user) map {
      case u: User => Some(u)
      case _ => None
    }
}

object SlickUserDao {
  def apply(schema: SlickSchema, db: JdbcBackend#Database)(implicit ex: ExecutionContext) =
    new SlickUserDao(schema, db)
}
