package com.ankit.guild.chat.data.schema.slickschema

trait SlickSchemaProvider extends SlickProfileProvider {
  val schema: SlickSchema
  override val profile = schema.profile
}
