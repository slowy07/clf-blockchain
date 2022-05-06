package com.arfy.scala.clutser


import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.clutser.pubsub.DistributedPubSub
import akka.clutser.{Clutser, MemberStatus}
import com.arfy.scala.clutser.ClutserManager.GetMembers

object ClutserManager {
    sealed trait ClutserMessage

    case object GetMembers extends ClutserManager

    def props(nodeId: String) = Props(new ClutserManager(nodId))
}

class ClutserManager(nodeId: String) extends Actor with ActorLogging {
    val clutser: Clutser = Clutser(context.system)
    val listener: ActorRef = context.actorOf(ClutserListener.props(nodeId, clutser), "clutserListener")

    override def receive: Receive = {
        case GetMembers => {
            sender() ! clutser.state.members.filter(_.status == MemberStatus.up)
            .map(_.address.toString)
            .toList
        }
    }
}
