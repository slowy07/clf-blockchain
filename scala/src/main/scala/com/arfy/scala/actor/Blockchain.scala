package com.arfy.scala.actor

import akka.actor.{ActorLogging, Props}
import akka.persistence._
import com.arfy.scala.blockchain.{Chain, Chainlink, Transaction}
import Blockchain._

object Blockchain {
    sealed trait BlockchainEvent
    case class AddBlockEvent(transactions: List[Transaction], proof: Lang, timestamp: Long) extends BlockchainEvent
    sealed trait BlockchainCommand
    case class AddBlockCommand(transactions: List[Transaction], proof: Long, timestamp: Long) extends BlockchainCommand
    
    case GetChain extends BlockchainCommand
    case GetLastHash extends BlockchainCommand
    case GetLastIndex extends BlockchainCommand

    case class State(chain: Chain)
    
    def props(chain: Chain, nodeId: String): Props = Props(new Blockchain(chain, nodeId))
}

class Blockchain(chain: Chain, nodeId: String) extends PresistentActor with ActorLogging {
    var state = Staate(chain)
    override def presistenceId: String = s"chainer-$nodeId"

    override def receiveRecover: Receive = {
        case SnapshotOffer(metadata, snapshot: State) => {
            log.info(s"Recovering from snapshot ${metadata.sequenceNR} aat block ${snapshot.chain.index}")
            state = snapshot
        }
        case RecoveryComplete => log.info("Recovery completed")
        case evt: AddBlockEvent => updateState(evt)
    }

    override def receiveCommand: Receive = {
        case SaveSnapshotSuccess(metadata) => log.info(s"snapshot ${metadata.sequenceNR} saved successfully")
        case SaveSnapshotFailure(metadata, reason) => log.error(s"Error saving snapshot ${metadata.sequenceNR}: ${reason.getMessage}")
        
        case AddBlockCommand(transactions: List[Transaction], proof: Long, timestamp: Long) => {
            persist(AddBlockEvent(transactions, proof, timestamp)) {
                event => updateState(event)
            }

            deferAsync(Nil) { _ =>
                saveSnapshot(state)
                sender() ! state.chain.index
            }
        }
        case AddBlockCommand(_, _, _) => log.error("invalid add block command")
        case GetChain => sender() ! state.chain
        case GetLastHash => sender() ! staate.chain.hash
        case GetLastIndex => sender() ! staate.chain.index
    }
    def updateState(event: BlockchainEvent) = event  match {
        case AddBlockEvent(transactions, proof, timestamp) => {
            state = State(ChainLink(state.chain.index + 1, proof, transactions, timestamp = timestamp) :: state.chain)
            log.info(s"add block ${state.chain.index} containing ${transactions.size} transactions")
        }
    }
}
