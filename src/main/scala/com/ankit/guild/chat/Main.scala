package com.ankit.guild.chat

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.ankit.guild.chat.data.dao.slickdao.{SlickMessageDao, SlickRoomDao, SlickUserDao}
import com.ankit.guild.chat.data.schema.slickschema.SlickSchema
import com.ankit.guild.chat.http.Server
import com.ankit.guild.chat.http.routes.{ChatRoutes, RoomRoutes, Routes, UserRoutes}
import com.ankit.guild.chat.service.{MessageService, RoomService, UserService}
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object Main extends App {
  val conf = ConfigFactory.load()

  implicit val system = ActorSystem(Behaviors.empty, "guild-chat-actor-system")
  implicit val exCtx = system.executionContext

  val dbProfile = sys.env.getOrElse("DB_PROFILE", { system.log.info("defaulting db profile"); "local.native.postgres" })

  val dc = DatabaseConfig.forConfig[JdbcProfile](dbProfile)
  val profile: JdbcProfile = dc.profile

  val db = dc.db

  val schema = SlickSchema(profile)

  // daos
  val roomDao = SlickRoomDao(schema, db)
  val userDao = SlickUserDao(schema, db)
  val messageDao = SlickMessageDao(schema, db)

  // services
  val roomService = RoomService(roomDao)
  val userService = UserService(userDao)
  val messageService = MessageService(messageDao)

  // routes
  val roomRoutes = RoomRoutes(roomService)
  val userRoutes = UserRoutes(userService)
  val chatRoutes = ChatRoutes(roomService, messageService)
  val routes = Routes(roomRoutes, userRoutes, chatRoutes)

  val server = Server(routes)

  server.start()
}
