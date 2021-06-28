package com.ankit.guild.chat.data.schema.slickschema

import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object SlickSchemaCreator extends App {
  val conf = ConfigFactory.load()

  val dc = DatabaseConfig.forConfig[JdbcProfile]("local.native.postgres")
  val profile: JdbcProfile = dc.profile

  val db = dc.db

  val schema = SlickSchema(profile)

  import profile.api._
  import schema._
  val tableQueries = List(users, rooms, messages)
  val schemas = tableQueries map { _.schema }
  val createActions = schemas map { _.create }

  createActions map { _.statements } foreach { println }

  Await.result(db.run(DBIO.seq(
    createActions :_*,
  )), 60.seconds)
}
