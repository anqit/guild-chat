package com.ankit.guild.chat.http.routes

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Flow, GraphDSL, Keep, Merge, MergeHub, Sink}
import akka.stream.{FlowShape, Materializer}
import com.ankit.guild.chat.http.sockets.SocketMessage.Error
import com.ankit.guild.chat.http.sockets.{SocketMessage, SocketMessageProcesser}
import spray.json._

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class ChatRoutes(val messageProcessor: SocketMessageProcesser)(implicit ex: ExecutionContext, mat: Materializer) extends RouteProvider with Directives {
  val incomingFlow: Flow[Message, SocketMessage, NotUsed] = Flow[Message].mapAsync(1) {
    case tm: TextMessage =>
      tm.toStrict(2.seconds).map(_.text)
        .map(_.parseJson)
        .map(SocketMessage.fromJsValue)
        .flatMap(messageProcessor.process)
        .recover(t => Error(t.getMessage))
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      Future.successful(SocketMessage.Error("not handling binary messages"))
  }

  val outgoingFlow: Flow[SocketMessage, Message, NotUsed] = Flow[SocketMessage]
    .map(SocketMessage.toJsValue)
    .map(SocketMessage.toWebSocketMessage)

  val (globalMerger, globalBroadcaster) = MergeHub.source[SocketMessage]
    .via(outgoingFlow)
    .toMat(BroadcastHub.sink[Message])(Keep.both).run()

  def getWebSocketFlow() = {
    val graph = GraphDSL.create(incomingFlow) { implicit b => processed =>
      import GraphDSL.Implicits._

      val privateFlow = b.add(Flow[SocketMessage].filter(sm => !SocketMessage.isGlobalResponse(sm)))
      val globalFlow = b.add(Flow[SocketMessage].filter(SocketMessage.isGlobalResponse))

      val splitter = b.add(Broadcast[SocketMessage](2))
      val merger = b.add(Merge[Message](2))

                          splitter.out(0) ~> globalFlow ~> globalMerger
                                                       globalBroadcaster ~> merger.in(0)
      processed ~> splitter
                          splitter.out(1) ~> privateFlow ~> outgoingFlow ~> merger.in(1)


      FlowShape(processed.in, merger.out)
    }

    Flow.fromGraph(graph)
  }

  private lazy val socket = pathEndOrSingleSlash {
    handleWebSocketMessages(getWebSocketFlow())
  }

  override lazy val route: Route = concat(
    socket
  )
}

object ChatRoutes {
  def apply(messageProcessor: SocketMessageProcesser)(implicit ex: ExecutionContext, mat: Materializer) =
    new ChatRoutes(messageProcessor)
}
