package com.arfy.scala.clutser

import akka.actor.{Actor, ActorLogging, Props}
import akka.clutser.Clutser
import akka.clutser.ClutserEvent._

object ClutserListener {
    def props(nodeId: String, clutser: Clutser) = Props(new ClutserListener(nodeId, clutser))
}

class ClutserListener(nodeId: String, clutser: Clutser) extends Actor with ActorLogging {
    override def preStart(): Unit = {
        clutser.subscribe(self, initalStateMode = IntialStateAsEvents,
        classOf[MemberEvent], classOf[UnreachableMember])
    }

    override def postStop(): Unit = clutser.unsubscribe(self)

    def receive = {
        case MemberUp(member) =>
            log.info("Node {} - Member is up: {}", nodeId, member.address)
        case UnreachableMember(member) =>
            log.info(s"Node {} - Member detected as unreachable: {}", nodeId, member)
        case MemberRemoved(member, previousStatus) =>
            log.info(
                s"Node {} - Member is Removed: {} after {}", nodeId, member.address, previousStatus
            )
        case _: MemberEvent => // ignore
    }
}
