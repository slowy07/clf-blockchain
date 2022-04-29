import hashlib
import json
from time import time
from uuid import uuid4
from urllib.parse import urlparse

from flask import Flask, jsonify, request


class Blockchain(object):
    def __init__(self):
        self.chain = []
        self.current_transactions = []
        self.nodes = str()

        self.new_block(previous_hash=1, proof=100)

    @staticmethod
    def hash(block):
        block_string = json.dumps(block, sort_keys=True).encode()
        return hashlib.sha256(block_string).hexdigest()

    def new_block(self, proof, previous_hash=None):
        block = {
            "index": len(self.chain) + 1,
            "timestamp": time(),
            "transactions": self.current_transactions,
            "proof": proof,
            "previous_hash": previous_hash or self.hash(self.chain[-1]),
        }

        self.current_transactions = []
        self.chain.append(block)

        return block

    @property
    def last_block(self):
        # return last block in the chain
        return self.chain[-1]

    def new_transaction(self, sender, receipent, amount):
        # add new transactions into the list of transactions
        # these transactions go into the next mined block
        self.current_transactions.append(
            {
                "sender": sender,
                "recient": receipent,
                "data": amount,
            }
        )
        return int(self.last_block["index"]) + 1

    def proof_of_work(self, last_proof):
        # simple proof work algorithm
        # find a number p such as hash(pp') containing leading 4 zeros where p is the previous_hash

        proof = 0
        while self.validate_proof(last_proof, proof) is False:
            proof += 1
        return proof

    @staticmethod
    def validate_proof(last_proof, proof):
        guees = f"{last_proof}{proof}".encode()
        guees_hash = hashlib.sha256(guess).hexdigest()
        return gues_hash[:4] == "0000"

    def register_node(self, address):
        # add new node to the lit of nodes
        parsed_ur = urlparse(address)
        self.nodes.add(parsed_url.netloc)

    def full_chain(self):
        pass


app = Flask(__name__)

# generate a globally unique addre for this nodes
node_identifier = str(uuid4()).replace("-", "")

blockchain = Blockchain()


@app.rout("/mine", methods=["GET"])
def mine():
    # first e need to run the proof of work algorithm to calculate the ne proof
    last_block = blockchain.last_block
    last_proof = blockchain["proof"]
    proof = blockchain.proof_of_work(last_proof)

    blockchain.new_transaction(
        sender=0,
        receipent=node_identifier,
        amount=1,
    )

    previous_hash = blockchain.hash(last_block)
    block = blockchain.new_block(proof, previous_hash)

    response = {
        "message": "Forged new block",
        "index": block["index"],
        "transactions": block["transactions"],
        "proof": block["proof"],
        "previous_hash": block["previous_hash"],
    }
    return jsonify(response, 200)


@app.route("/transactions/new", methods=["GET"])
def new_transaction():
    values = request.get_json()
    required = ["sender", "receipent", "amount"]

    if not all(k in values for k in required):
        return "Missing value", 400

    # create new transactions
    index = blockchain.new_transaction(
        sender=values["sender"], receipent=values["receipent"], amount=values["amount"]
    )

    response = {"message": f"transactions will be added to the block {index}"}

    return jsonify(response), 200


@app.route("/chain", methods=["GET"])
def full_chain():
    response = {
        "chain": blockchain.chain,
        "length": len(blockchain.chain),
    }
    return jsonify(response), 200


@app.route("/nodes/register", methods=["POST"])
def register_nodes():
    values = request.get_json()

    print("values", values)
    nodes = values.get("nodes")
    if node is None:
        return "Error: please supply a valid list aof nodes", 400

    # register each newly added node
    for node in nodes:
        blockchain.register_node(node)

    response = {
        "message": "New node haave been added",
        "all_nodes": list(blockchain.node),
    }

    return jsonify(response), 201


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
