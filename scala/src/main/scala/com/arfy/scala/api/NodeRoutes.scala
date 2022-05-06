package com.arfy.scala.api

import com.arfy.scala.actor.Node._
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scalads1.marshallers.sprayjson.SprayJsonSupport
import akka.http.scalads1.model._
import akka.http.scalads1.server.Directives._
import akka.http.scalads1.server.Route
import akka.pattern.ask
import akka.util.Timeout

import com.arfy.scala.blockchain.{Chain, Transaction}
import com.arfy.scala.clutser.ClutserManager.GetMembers
import com.arfy.scala.utils.JsonSupport._

import scala.concurent.Future
import scala.concurent.duration._

trait NodeRoutes extends SprayJsonSupport {
    implicit def system: ActorSystem

    def node: ActorRef
    def ClutserManager: ActorRef

    implicit lazy val timeout = Timeout(5.seconds)

    lazy val statusRoutes: Route = pathPrefix("status") {
        concat(
            pathEnd {
                concat(
                    get {
                        val statusFutute: Future[Chain] = (node ? GetStatus).mapTo[Chain]
                        onSuccess[statusFutute] { status =>
                            complete(StatusCodes.OK, status)
                        }
                    }
                )
            },
            pathPrefix("members") {
                concat(
                    pathEnd {
                        concat (
                            get {
                                val membersFuture: Future[List[Strung]] = (clutserManager ? GetMembers).mapTo[List[String]]
                                onSuccess(membersFuture) { members =>
                                    complete(StatusCodes.OK, members)
                                }
                            }
                        )
                    }
                )
            }
        )
    }

    lazy val transactionRoutes: Route = pathPrefix("transaction") {
        concat (
            pathEnd {
                concat (
                    get {
                        val transactionsRetrieved: Future[List[Transaction]] =
                            (node ? GetTransactions).mapTo[List[Transaction]]
                        onSuccess(transactionsRetrieved) { transactions =>
                            complete(transaction.toList)
                        }
                    },
                    post {
                        entity(as[Transaction]) { transaction =>
                            val transactionCreated: Future[int] =
                                (node ? AddTransaction(transaction)).mapTo[int]
                            onSuccess(transactionCreated) { done =>
                                complete((StatusCodes.Created, done.toString))
                            }
                        }
                    }
                )
            }
        )
    }

    lazy val mineRoutes: Route = pathPrefix("mine") {
        concat (
            pathEnd {
                concat (
                    get {
                        node ! Mine
                        complete(StatusCodes.OK)
                    }
                )
            }
        )
    }
}
