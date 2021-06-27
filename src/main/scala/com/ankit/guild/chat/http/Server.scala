package com.ankit.guild.chat.http

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import com.ankit.guild.chat.http.routes.RouteProvider

import scala.concurrent.ExecutionContext
import scala.io.StdIn

class Server(routeProvider: RouteProvider, interface: String = "0.0.0.0", port: Int = 8080)(implicit system: ActorSystem[Nothing], exCtx: ExecutionContext) extends Directives {
  def start() = {
    val bindingFuture = Http().newServerAt(interface, port).bind(routeProvider.route)

    println(s"[guild-chat] Server online at $interface:$port\n[guild-chat] Press RETURN to terminate")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
  }
}

object Server {
  def apply(routeProvider: RouteProvider)(implicit system: ActorSystem[Nothing], exCtx: ExecutionContext) =
    new Server(routeProvider)
}
