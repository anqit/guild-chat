package com.ankit.guild.chat.http.routes

import akka.http.scaladsl.server.Directives
import com.ankit.guild.chat.service.RoomService
import com.ankit.guild.chat.http.json.JsonSupport._

class RoomRoutes(val roomService: RoomService) extends RouteProvider with Directives {
  lazy val getRoomsRoute = (pathEndOrSingleSlash & get) {
    complete {
      roomService.getRooms()
    }
  }

  override lazy val route = concat(
    getRoomsRoute
  )
}

object RoomRoutes {
  def apply(roomService: RoomService) = new RoomRoutes(roomService)
}
