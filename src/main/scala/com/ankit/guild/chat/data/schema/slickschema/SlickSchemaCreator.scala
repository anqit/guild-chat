package com.ankit.guild.chat.data.schema.slickschema

import com.typesafe.config.ConfigFactory
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.jdbc.meta.MTable

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.duration.{Duration, DurationInt}

object SlickSchemaCreator {
  val conf = ConfigFactory.load()
    val dc = DatabaseConfig.forConfig[JdbcProfile](sys.env.getOrElse("DB_PROFILE", "local.native.postgres"))
    val profile: JdbcProfile = dc.profile

    val db = dc.db
    val schema = SlickSchema(profile)

    import profile.api._
    import schema._

  def create() = {
    val tableQueries = List(users, rooms, messages)
    val schemas = tableQueries map {
      _.schema
    }
    val createActions = schemas map {
      _.create
    }

    createActions map {
      _.statements
    } foreach {
      println
    }

    Await.result(db.run(DBIO.seq(
      createActions: _*,
    )), 60.seconds)
  }

  def createIfNotExists()(implicit ex: ExecutionContext) = {
    Await.result(db.run(DBIO.seq(
      MTable.getTables map (tables => {
        if (!tables.exists(_.name.name == schema.users.baseTableRow.tableName)) {
          println("creating schema")
          create()
        } else {
          println("schema exists already")
        }
      })
    )), Duration.Inf)
  }
}
