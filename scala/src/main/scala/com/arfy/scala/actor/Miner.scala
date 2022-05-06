package com.arfy.scala.actor

import akka.actor.Status.{Failure, Success}
import akka.actor.{Actor, ActorLogging, Props}
import com.arfy.scala.actor.Miner._
import com.arfy.scala.exception.{InvalidProofException, MinerBusyException}
import com.arfy.scala.proof.ProofOfWork

import scala.concurrent.Future

object Miner {
    sealed trait MinerMessage

    case class Validate(hash: String, proof: Long) extends MinerMessage
    case class Mine(hash: String) extends MinerMessage
    case object Ready extends MinerMessage
    
    val props: Props = Props(new Miner)
}

class Miner extends Actor with ActorLogging {
    import context._
    
    def validate: Receive = {
        case Validate(hash, proof) => {
            log.info(s"validating proof $proof")
            if (ProofOfWork.validProof(hash, proof)) {
                log.info("proof is valid")
                ender() ! Success
            } else {
                log.info("proof is invalid")
                sender() ! Failure(new InvalidProofException(hash, proof))
            }
        }
    }
    
    def ready: Receive = validate orElse {
        case Mine(hash) => {
            log.info(s"mining hash $hash...")
            val proof = Future {
                ProofOfWork.proofOfWork(hash)
            }
            sender() ! proof
            become(busy)
        }
        
        case Ready => {
            log.info("ready to mine!")
            sender() ! Success("OK")
        }
    }

    def busy: Receive = validate orElse {
        case Mine(_) => {
            log.info("ready to mining")
            sender ! Failure(new MinerBusyException("miner is busy"))
        }

        case Ready => {
            log.info("ready to mine a new block")
            become(ready)
        }
    }

    override def receive: Receive = {
        case Ready => become(reaady)
    }
}
