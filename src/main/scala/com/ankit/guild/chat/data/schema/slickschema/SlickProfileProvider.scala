package com.ankit.guild.chat.data.schema.slickschema

import slick.jdbc.JdbcProfile

trait SlickProfileProvider {
  val profile: JdbcProfile
}
