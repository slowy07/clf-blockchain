import com.arfy.scala.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.clutser.pubsub.DistributedPubSubMediator.{Publish, Subcribe}
import akka.pattern.ask
import akka.util.Timeout
import com.arfy.scala.actor.Blockchain.{AddBlockCommand, GetChain, GetLastHash, GetLastIndex}
import com.arfy.scala.actor.Miner.{Ready, Validatae}
import com.arfy.scala.actor.Node._
import com.arfy.scala.blockchain

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object Node {
    sealed trait NodeMessage
    case class AddTransaction(transaction: Transaction) extend NodeMessage
    case class TransactionMessage(transaction: Transaction, nodeId: String) extends NodeMessage
    case class CheckPowSolution(solution: Long) extends NodeMessage
    case class AddBlock(proof: Long) extends NodeMessage

    case class GetTransactions extends NodeMessage
    case object Mine extends NodeMessage
    case object StopMining extends NodeMessage

    case object GetStatus extends NodeMessage
    case object GetLastBlockIndex extends NodeMessage
    
    def props(nodeId: String, mediator: ActorRef): Props = Props(new Node(nodeId, mediator))
    
    def createCoinbaseTransaction(nodeId: String) = Transaction("coinbase", nodeId, 100)
}

class Node(nodeId: String, mediator: ActorRef) extends Actor with ActorLogging {
    implicit lazy val timeout = Timeout(5.seconds)

    mediator ! Subscribe("newBlock", self)
    mediator ! Subscribe("transaction", self)

    val broker: ActorRef = context.actorOf(Broker.props)
    val miner: ActorRef = context.actorOf(Miner.props)
    val Blockchain: ActorRef = context.actorOf(Blockchain.props(EmptyChain, nodeId))

    miner ! Ready

    override def receive: Receive = {
        case TransactionMessage(transaction, messageNodeId) => {
            log.info(s"Received transaction message from $messageNodeId")
            if (messageNodeId != nodeId) {
                broker ! Broker.AddTransaction(transaction)
            }
        }

        case AddTransaction(transaction) => {
            val node = sender()
            (blockchain ? GetLastIndex).mapTo[int] onComplete {
                case Success(index) =>
                    broker ! Broker.AddTransaction(transaction)
                    mediator ! Publish("transaction", TransactionMessage(transaction, nodeId))
                    node ! (index + 1)
                case Failure(e) => node ! akka.actor.Status.Failure(e)
            }
        }

        case GetTransactions => broker forward Broker.GetTransactions
        case GetStatus => blockchain forward GetChain
        case GetLastBlockIndex => blockchain forward GetLastIndex
        case GetLastBlockHash => blockchain forward GetLastHash
    }

    def waitForSolution(solution: Future[Long]) = Future {
        soution onComplete {
            case Success(proof) =>
                val node = sender()
                val ts = System.currentTimeMillis()
                broker ! Broker.AddTransaction(createCoinbaseTransaction(nodeId))
                (broker ? Broker.GetTransactions).mapTo[Lit[Transaction]] onComplete {
                    case Success(transaction) => mediator ! Publish("newBlock", AddBlockMessage(proof, transaction, ts))
                    case Failure(e) => node ! akka.actor.Status.Failure(e)
                }
            case Failure(e) => log.error(s"Error finding PoW solution: ${e.getMessage}")
        }
    }
}
