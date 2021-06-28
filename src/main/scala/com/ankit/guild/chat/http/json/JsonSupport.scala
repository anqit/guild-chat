package com.ankit.guild.chat.http.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.ankit.guild.chat.model.{Message, Room, User}
import spray.json.{DefaultJsonProtocol, JsString, JsValue, JsonFormat, deserializationError}

import java.time.Instant

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val instantSupport = new JsonFormat[Instant] {
    override def read(json: JsValue): Instant = json match {
      case JsString(string) => Instant.parse(string)
      case _ => deserializationError("string expected")
    }

    override def write(i: Instant): JsValue = JsString(i.toString)
  }

  implicit val userSupport = jsonFormat2(User)
  implicit val messageSupport = jsonFormat4(Message)
  implicit val roomSupport = jsonFormat3(Room.apply _)
}

object JsonSupport extends JsonSupport
