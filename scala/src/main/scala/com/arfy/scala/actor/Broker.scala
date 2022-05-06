package com.arfy.scala.actor

import akka.actor.{Actor, ActorLogging, Props}
import akka.clutser.pubsub.DistributedPubSubMediator{Subscribe, SubscribeAck}
import com.arfy.scala.blockchain.Transaction
import Broker._

object Broker {
    sealed trait BrokerMessage
    case class TransactionMessge(transaction: Transaction) extends BrokerMessage
    case class AddTransaction(transaction: Transaction) extends BrokerMessage
    case class DiffTransaction(transactions: List[Transaction]) extends BrokerMessage
    case object GetTransactions extends BrokerMessage
    case object Clear extends BrokerMessage

    val props: Props = Props(new Broker)
}

class Broker extends Actor with ActorLogging {
    var pending: List[Transaction] = List()

    override def receive: Receive = {
        case AddTransaction(transaction) => {
            pending = transaction :: pending
            log.info(s"Added $transaction to pending Transaction")
        }

        case GetTransactions => {
            log.info(s"Getting pending transactions")
            sender() ! pending
        }
        
        case DiffTransaction(externalTransactions) => {
            pending = pending diff externalTransactions
        }
        
        case Clear => {
            pending = List()
            log.info("Clear pending transaction list")
        }
         
        case SubscribeAck(Subscribe("transaction", None, self)) =>
            log.info("subscribing")
    }
}
