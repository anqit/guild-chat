package com.ankit.guild.chat.http.routes


import akka.http.scaladsl.server.Route

trait RouteProvider {
  def route: Route
}

object RouteProvider {
  implicit def toRoute(rp: RouteProvider): Route = rp.route
}
