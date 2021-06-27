package com.ankit.guild.chat.http.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.ankit.guild.chat.model.User
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val userSupport = jsonFormat1(User)
}

object JsonSupport extends JsonSupport
