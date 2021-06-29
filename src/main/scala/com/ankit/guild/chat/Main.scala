package com.ankit.guild.chat

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import com.ankit.guild.chat.data.dao.slickdao.{SlickMessageDao, SlickRoomDao, SlickUserDao}
import com.ankit.guild.chat.data.schema.slickschema.{SlickSchema, SlickSchemaCreator}
import com.ankit.guild.chat.http.Server
import com.ankit.guild.chat.http.routes.{ChatRoutes, RoomRoutes, Routes, UserRoutes}
import com.ankit.guild.chat.http.sockets.SocketMessageProcesser
import com.ankit.guild.chat.service.{MessageService, RoomService, UserService}
import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

object Main extends App {
  val conf = ConfigFactory.load()

  implicit val system = ActorSystem(Behaviors.empty, "guild-chat-actor-system")
  implicit val exCtx = system.executionContext

  SlickSchemaCreator.createIfNotExists()

  val dbProfile = sys.env.getOrElse("DB_PROFILE", { system.log.info("defaulting db profile"); "local.native.postgres" })

  val dc = DatabaseConfig.forConfig[JdbcProfile](dbProfile)
  val profile: JdbcProfile = dc.profile

  val db = dc.db

  val schema = SlickSchema(profile)

  // daos
  val userDao = SlickUserDao(schema, db)
  val messageDao = SlickMessageDao(schema, db)
  val roomDao = SlickRoomDao(schema, db, messageDao)

  // services
  val roomService = RoomService(roomDao)
  val userService = UserService(userDao)
  val messageService = MessageService(messageDao)

  val socketMessageProcesser = SocketMessageProcesser(roomService, messageService)

  // routes
  val roomRoutes = RoomRoutes(roomService)
  val userRoutes = UserRoutes(userService)
  val chatRoutes = ChatRoutes(socketMessageProcesser)
  val routes = Routes(roomRoutes, userRoutes, chatRoutes)

  val server = Server(routes)

  server.start()
  db.close()
}
