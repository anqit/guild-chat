package com.ankit.guild.chat.http.routes

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink}
import com.ankit.guild.chat.http.sockets.{SocketMessageProcesser, SocketMessage}
import com.ankit.guild.chat.service.{MessageService, RoomService}
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class ChatRoutes(val roomService: RoomService, val messageService: MessageService)(implicit ex: ExecutionContext, mat: Materializer) extends RouteProvider with Directives {
//  val roomSinkMap: scala.collection.mutable.Map[Int, (Sink[TextMessage, NotUsed], Source[TextMessage, NotUsed])] = scala.collection.mutable.Map()
  val messageProcessor = new SocketMessageProcesser(roomService, messageService)

  val messageFlow = Flow[Message].mapAsync(1) {
      case tm: TextMessage =>
        tm.toStrict(2.seconds).map(_.text)
          .map(_.parseJson)
          .map(SocketMessage.fromJsValue)
          .flatMap(messageProcessor.process)
          .map(SocketMessage.toJsValue)
          .map(SocketMessage.toWebSocketMessage)
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        Future.successful(SocketMessage.Error("not handling binary messages"))
          .map(SocketMessage.toJsValue)
          .map(SocketMessage.toWebSocketMessage)
    }

  val (sink, source) = MergeHub.source[Message].via(messageFlow).toMat(BroadcastHub.sink[Message])(Keep.both).run()
/*
  def createRoom(room: Room) = {
    val newRoom = roomService.createRoom(room) map {
      case Some(Room(_, Some(roomId))) =>
        val bcSink = BroadcastHub.sink[TextMessage]
        val mhSource = MergeHub.source[TextMessage]
        val (sink, source) = mhSource.via(messageFlow).toMat(bcSink)(Keep.both).run()
        roomSinkMap += (roomId, (sink, source))

      case _ => throw new Exception("couldn't create room")
    }
  }*/

  val socketHandler = Flow.fromSinkAndSource(sink, source)

  lazy val socket = pathEndOrSingleSlash {
    handleWebSocketMessages(socketHandler)
  }

  override lazy val route = concat(
    socket
  )
}

object ChatRoutes {
  def apply(roomService: RoomService, messageService: MessageService)(implicit ex: ExecutionContext, mat: Materializer) =
    new ChatRoutes(roomService, messageService)
}
