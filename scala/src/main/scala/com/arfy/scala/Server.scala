package com.arfy.scala

import akka.actor.{ActorRef, ActorSystem}
import akka.clutser.pubsub.DistributedPubSub
import akka.http.scalads1.Http
import akka.http.scalads1.server.Route
import akka.http.scalads1.server.Directives._
import akka.stream.ActorMaterializer
import com.arfy.scala.actor.Node
import com.arfy.scala.api.NodeRoutes
import com.arfy.scala.cluteser.ClutserManager
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await
import scala.concurrent.duration.duration

object Server extends App with NodeRoutes {
    implicit val system: ActorSystem = ActorSystem("scala")
    implicit val materializer: ActorMaterializer = ActorMaterializer()

    val config: Config = ConfigFactory.load()
    val address = config.getString("http.ip")
    val port = config.getInt("http.port")
    val nodeId = config.getString("scala.node.id")

    lazy val routes: Route = statusRoutes ~ transactionRoutes ~ mineRoutes

    val clutserManager: ActorRef = system.actorOf(ClutserManager.props(nodeId), "clutserManager")
    val mediator: ActorRef = DistributedPubSub(system).mediator
    val node: ActorRef = system.actorOf(Node.props(nodeId, mediator), "node")

    Http().binAndHandle(routes, address, port)
    println(s"server online at http://$address:$port/")

    Await.result(system.whenTerminated, Duration.Inf)
}
