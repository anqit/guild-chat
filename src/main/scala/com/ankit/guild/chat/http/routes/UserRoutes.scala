package com.ankit.guild.chat.http.routes

import akka.http.scaladsl.marshalling.ToResponseMarshaller
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.FromRequestUnmarshaller
import com.ankit.guild.chat.http.json.JsonSupport._
import com.ankit.guild.chat.model.User
import com.ankit.guild.chat.service.UserService

class UserRoutes(userService: UserService)(implicit marshaller: ToResponseMarshaller[User], unmarshaller: FromRequestUnmarshaller[User]) extends RouteProvider with Directives {
  lazy val getOrCreateRoute = (pathEndOrSingleSlash & put & entity(as[User])) { user => {
    complete(userService.getOrCreate(user))
  }}

  override def route = concat(getOrCreateRoute)
}

object UserRoutes {
  def apply(userService: UserService) = new UserRoutes(userService)
}
