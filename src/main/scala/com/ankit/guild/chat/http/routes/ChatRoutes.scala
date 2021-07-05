package com.ankit.guild.chat.http.routes

import akka.NotUsed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.scaladsl.{Broadcast, BroadcastHub, Flow, GraphDSL, Keep, Merge, MergeHub, Sink, Source}
import akka.stream.{FlowShape, Materializer, SourceShape}
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
    case bm: BinaryMessage =>
      bm.dataStream.runWith(Sink.ignore)
      Future.successful(SocketMessage.Error("not handling binary messages"))
  }

  def outgoingFlow(filter: SocketMessage => Boolean): Flow[SocketMessage, Message, NotUsed] = Flow[SocketMessage]
    .filter(filter)
    .recover(t => Error(t.getMessage))
    .map(SocketMessage.toJsValue)
    .map(SocketMessage.toWebSocketMessage)

  //        incoming      internal processed message source
  val (globalMergingSink, internalBroadcastingSource) = MergeHub.source[Message]
    .via(incomingFlow)
    .toMat(BroadcastHub.sink[SocketMessage])(Keep.both).run()

  //       global messages
  val globalBroadcastingSource = internalBroadcastingSource
    .via(outgoingFlow(SocketMessage.isGlobalResponse))
    .toMat(BroadcastHub.sink[Message])(Keep.right).run()

  def getCombinedSource() = {
    val graph = GraphDSL.create() { implicit b =>
      import GraphDSL.Implicits._

      // private messages
      val privateFlow = b.add(outgoingFlow(sm => !SocketMessage.isGlobalResponse(sm)))
      val merger = b.add(Merge[Message](2))

      internalBroadcastingSource ~> privateFlow ~> merger.in(0)
      globalBroadcastingSource ~> merger.in(1)

      SourceShape(merger.out)
    }

    Source.fromGraph(graph)
  }

  private lazy val socket = pathEndOrSingleSlash {
    handleWebSocketMessages(Flow.fromSinkAndSource(globalMergingSink, getCombinedSource()))
  }

  override lazy val route: Route = concat(
    socket
  )
}

object ChatRoutes {
  def apply(messageProcessor: SocketMessageProcesser)(implicit ex: ExecutionContext, mat: Materializer) =
    new ChatRoutes(messageProcessor)
}
