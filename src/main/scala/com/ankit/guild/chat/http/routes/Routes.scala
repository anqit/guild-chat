package com.ankit.guild.chat.http.routes

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives
import akka.stream.Attributes.LogLevels

class Routes(val roomRoutes: RoomRoutes, val userRoutes: UserRoutes, val webSocketRoutes: ChatRoutes) extends RouteProvider with Directives {
  lazy val healthCheckRoute = path("ping") {
    get {
      complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, "pong"))
    }
  }

  override lazy val route = logRequestResult("top-level", LogLevels.Info) {
    concat(
      healthCheckRoute,
      pathPrefix("rooms") { roomRoutes },
      pathPrefix("users") { userRoutes },
      pathPrefix("chat") { webSocketRoutes },
    )
  }
}

object Routes {
  def apply(roomRoutes: RoomRoutes, userRoutes: UserRoutes, webSocketRoutes: ChatRoutes) =
    new Routes(roomRoutes, userRoutes, webSocketRoutes)
}
