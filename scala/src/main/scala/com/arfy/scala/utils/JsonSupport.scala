package com.arfy.scala.utils

import com.arfy.scala.blockchaib.{Chain, ChainLink, EmptyChain, Transaction}
import spray.json._

object JsonSupport extends DefaultJsonProtocol {
    implicit objet TransactionJsonFormat extends RootJsonFormat[Transaction] {
        def write(t: Transaction) = JsObject (
            "sender" -> JsString(t.sender),
            "recipient" -> JsString(t.recipient),
            "value" -> JsNumber(t.value),
        )

        def read(value: JsValue) = {
            value.asJsObject.getFields("sender", "recipient", "value") match {
                case Seq(JsString(sender), JsString(recipient), JsNumber(amount)) =>
                    new Transaction(sender, recipient, amount.toLong)
                case _ => throw new DeserializationException("Transaction expected")
            }
        }
    }

    implicit object ChainLinkJsonFormat extends RootJsonFormat[ChainLink] {
        override def read(json: JsValue): ChainLink = json.asJsObject.getFields("index", "proof", "values", "previousHash", "timestamp", "tail") match {
            case Seq(JsNumber(index), JsNumber(proof), values, JsString(previousHash), JsNumber(timestamp), tail) =>
                ChainLink(index.toInt, proof.toLong, values.converTo[List[Transaction]], previousHash, timestamp.toLong, tail.convertTo(ChainJsonFormat))
            case _ => throw new DeserializationException("cannot deseriliaze: Chainlink expected")
        }

        override def write(obj: ChainLink): JsValue = JsObject (
            "index" -> JsNumber(obj.index),
            "proof" -> JsNumber(obj.proof),
            "values" -> JsArray(obj.values.map(_.toJson).toVector),
            "previousHash" -> JsString(obj.previousHash),
            "timestamp" -> JsNumber(obj.timestamp),
            "tail" -> ChainJsonFormat.write(obj.tail)
        )
    }

    implicit object ChainJsonFormat extends RootJsonFormat[Chain] {
        def write(obj: Chain): JsValue = obj match {
            case linkL ChainLink => link.toJson
            case EmptyChain => JsObject(
                "index" -> JsNumber(EmptyChain.index),
                "hash" -> JsString(EmptyChain.hash),
                "value" -> JsArray(),
                "proof" -> JsNumber(EmptyChain.proof),
                "timeStamp" -> JsNumber(EmptyChain.timeStamp)
            )
        }

        def read(json: JsValue): Chain = {
            json.asJsObject.getFields("previousHash") match {
                case Seq(_) => json.convertTo[ChainLink]
                case Seq() => EmptyChain
            }
        }
    }
}
