## building clf-blockchain

each block has an index, timestamp, transcation, proof (more on thath later) and a hash of the previous transcation.
here is an example of what a single block looks like

```json
block = {
    'index': 1,
    'timestamp': 1506092455,
    'transactions': [
        {
            'sender': "852714982as982341a4b27ee00",
            'recipient': "a77f5cdfa2934hv25c7c7da5df1f",
            'amount': 5,
        }
    ],
    'proof': 323454734020,
    'previous_hash': "2cf24dba5fb0a3202h2025c25e7304249898"
}
```

## blockchain represenging

we create a blockchain class whose constructor creates a list to store our blockchain and another to store transactions. here is how the class will look like

```python
class Blockchain(object):
  def __init__(self):
    self.chains = []
    self.current_transactions = []

  @staticmethod
  def hash(block):
    pass

  def new_block(self):
    pass

  @property
  def last_block(self):
    return self.chain[-1]
```

this blockchain class is responsible for managing the chain. it will store transactions and have helper functions.

the ``new_block`` method will create a new block and adds it on the chain and returns the last block in the chain.

the ``last_block`` will return the last block in the chain.

each block contains the hah and the hash the previous block.this is what gives blockhcin it's immutability i.e if anyone attack this, all subsequent blocks will be corrupt, it's the core idea of blockchains.

## adding transactions to the blocks

we will need some way of adding transaction to the block.

```python
class Blockchain(object):
  ...
  def new_transaction(self, sender, receipent, amount):
    self.current_transactions.append({
      'sender': sender,
      'receipent': receipent,
      'amount': amount,
    })
    return self.last_block['index'] + 1
```

the ``new_transaction`` returns index of the block which will be added to ``current_transactions`` and is next one to be mined..

## creating new blocks 

is addition to creating the genesis block in our constructor, we will alo need to flesh out method for the ``new_block()``, ``add_new_transaction()`` and ``hash()``.

```python
import hashlib
import json
import time

class Blockchain(object):
  def __init__(self):
    self.chain = []
    self.current_transactions = []
    self.new_block(previous_hash = 1, proof = 100)

  @staticmethod
  def hash(block):
    block_string = json.dumps(block, sort_keys = True)
    return hashlib.sha256(block_string).hexdigest()

  def new_block(self, proof, previous_hash = None):
    # create a new block in the blockchain
    block = {
      'index': len(self.chain) + 1,
      'timestamp': time.time(),
      'transcations': self.current_transactions,
      'proof': proof,
      'previous_hash': previous_hash or self.hash(self.chain[-1])
    }

    self.current_transactions = []
    self.chain.append(block)
    return block

  @property
  def last_block(self):
    # return last block in the chains
    return self.chain[-1]

  def new_transaction(self, sender, receipent, amount):
    self.current_transactions.append({
      "sender": sender,
      "receipent": receipent,
      "data": amount
    })
    return int(self.last_block['index']) + 1
```
once block is initiated, we need to feed it with the genesis block (a block ioth no predecessors). e ill also need to add _a proof of work_ to our genesis block which is the result of mining.

at this point, we're nearly done representing our blockchain


## understanding proof of work

a proof of work algorithm are how new blocks are created or mined on the blockchain.the goal is to discover number that solves problem. the number must be difficult and resources consuming to find but super quick and easyto verify.

let say that hash some integer x multipled by another y must always end in 0. so, as an example, the ``hash(x * y) = 4b4f4b4f54...0``

```python
from hashlib import sha256

x = 5
y = 0

while sha256(f'{x * y}'.encode()).hexdigest()[-1] != "0":
  y + 1

print(f"solution is y = {y}")
```
in this example we fixed the ``x = 5``.the solution in this case is ``x = 5 and y = 21`` since it produced hash ``0``

```
hash(5 * 21) = "1253e9373e781b7500266caa55150e08e210bc8cd8cc70d89985e3600155e860"
```

in the bitcoin world, the proof of work algorithm is called ``hashcash``. and it's not any different from the example above. it's the very algorithm that miner race to solve in order to create new block. the difficult i of course determined by the number of the characters searched for in the string. in our example we simplified it by defining that resultant hash must end in 0 to make the whole thing in our case quicker and less resource intensive but this is ho it works really.

the miners are rewarded for finding a solution by receiving coin. in a transaction. there are many opinions on effectiness of this but this is how it works. and it really is taht simple and this ay network is able to easily verify their solution.

## implementing proof of work

implementing a similiar algorithm for our blockchain. our rule will be similar to the example above.

_find a number p thath when hashed with the previous block's solution a hah with 4 leading 0 is produced_

```python
import hashlib
import json

from time import time
from uuid import uuid4

class Blockchain(object):
  ...
  def proof_of_ork(self, last_proof):
    proof = 0
    while self.valid_proff(last_proof, proof) is False:
      proof += 1
    return proof

  @staticmethod
  def validate_proof(last_proof, proof):
    guess = f"{last_proof}{proof}".encode()
    guess_hash = hashlib.sha256(guess).hexdigest()
    return guess_hash[:4] == "0000"
```

to adjust the difficulty of the algorithm, we could modify the number of leading zeors. But strictly speaking 4 is sufficient enough. also, you may find out that adding an extra 0 makes a mammoth difference to the time required to find a solution.

now, our Blockchain class is pretty much complete, let's begin to interact with the ledger using the HTTP requests.

## blockchain as an API

python flask framework. it's a micro-framwoek and it's really easy to use o for our example it'll do nicely.

create three simple API endpoints:

- ``/transaction/new`` to create a new transaction block
- ``/mine`` to tell our service to mine a new blocks
- ``/chain`` to return the full blockchain

our server will from a single node in our Blockchain 

```python
import hashlib
import json
from time import time
from uuid import uuid4

from flash import Flask, jsonify, request

# initiate node
app = Flask(__name__)

# generate a globally unique address for this node
node_identifier = str(uuid4()).replace('-', '')

# initiate the blockchain
blockchain = Blockchain()

@app.route('/mine', method=['GET'])
def mine():
  return "mine a new block"

@app.route('/transaction/new', methods=['GET'])
def transcation_new():
  return "add a new transcation"

@app.route('/chain', methods=['GET'])
def chain():
  response = {
    'chain': blockchain.chain,
    'lenght': len(blockchain.chain),
  }
  return jsonify(response), 200

if __name__ == "__main__":
  app.run(host = "0.0.0.0", 5000)
```

this is what the requesst for the transcation will look like. it's whath the user will send to the server

```python
{
  "sender": "sender_address",
  "recipient": "recipient_address",
  "amount": 100
}
```
since we already have the method for adding transaction to a block, the rest is easy and pretty straight forward

```python
import hashlib
import json

from time import time
from uuid import uuid4
from flask import Flask, jsonify, request

...
@app.route('/transactions/new', method=['POS'])
def new_transaction():
  values = request.get_json()
  required = ['sender', 'recipient', 'amount']
  
  if not all(k in values for k in required):
    return "missing values", 400

  # create new transaction
  index = blockchain.add_new_transaction(values["sender"], values['recipient'], values['amount'])
  response = {
    'message': f"transaction will be added to the block {index}"
  }
  
  return jsonify(response), 200
```

## the mining endpoint

our mining endpoint is where the mining happens and it's actually very easy as all it has to do are three things:

- calculate proof of work
- reward the miner by adding a transaction granting miner 1 coin
- forge the new block by adding it to the chain.

```python
import hashlib
import json

from time import time
from uuid import uuid4
from flask import Flask, jsonify, request

...
@app.route('/mine', method=['GET'])
def mine():
  # first we have to run the proo of work algorithm
  # to calculte the new proff
  last_block = blockchain.last_block
  last_proof = last_block['proof']
  proof = blockchain.proof_of_work(last_proof)

  # we must receive reward for finding the proof
  blockchain.new_transaction(
    sender = 0,
    recipient = node_identifier,
    amount = 1,
  )

  # forge the new block by adding it to the data
  previous_hash = blockchain.hash(last_block)
  block = blockchain.new_block(proof, previous_hash)

  response = {
    'mesage': "forged new block",
    'index': block['index'],
    'transaction': block['transaction'],
    'proof': block['proof'],
    'previous_hash': block['previous_hash'],
  }

  return jsonify(response), 200
```

at this point, we are done, adn we can start interacting with out blockchain

## interacting with our blockchain

you can use  plain old url or postman to interact with our blockchain API over the network

```
$ python3 blockchain.py
* Running on http://127.0.0.1:5000/ (Press CTRL+C to quit)
```

so first off let's try mining a block by making GET requesst to the "mine" 

```json
[
  {
    "index": 1, 
    "message": "Forged new block.", 
    "previous_hash": "7cd122100c9ded644768ccdec2d9433043968352e37d23526f63eefc65cd89e6", 
    "proof": 35293, 
    "transactions": [
      {
        "data": 1, 
        "recipient": "6a01861c7b3f483eab90727e621b2b96", 
        "sender": 0
      }
    ]
  }, 
  200
]
```

lets create a new transaction by making ``post`` request to ``localhosst:5000/transaction/new`` with body containing our transaction structure

```
$ curl -X POST -H "Content-Type: application/json" -d '{
 "sender": "d4ee26eee15148ee92c6cd394edd974e",
 "recipient": "recipient-address",
 "amount": 5
}' "http://localhost:5000/transactions/new"
```

i have restarted the server, mine two blocks, to given 3 in total. lets inspect the full chain by requesting ``localhost:5000/chain``

```json
{
  "chain": [
    {
      "index": 1,
      "previous_hash": 1,
      "proof": 100,
      "timestamp": 1506280650.770839,
      "transactions": []
    },
    {
      "index": 2,
      "previous_hash": "c099bc...bfb7",
      "proof": 35293,
      "timestamp": 1506280664.717925,
      "transactions": [
        {
          "amount": 1,
          "recipient": "8bbcb347e0631231...e152b",
          "sender": "0"
        }
      ]
    },
    {
      "index": 3,
      "previous_hash": "eff91a...10f2",
      "proof": 35089,
      "timestamp": 1506280666.1086972,
      "transactions": [
        {
          "amount": 1,
          "recipient": "9e2e234e12e0631231...e152b",
          "sender": "0"
        }
      ]
    }
  ],
  "length": 3
}
```

## transaction verification

for this we will usign Python NaCl to generate a ``public/private`` signing key pair which need to be generated before runtime. we will employ the cryptography using the ``Public-key`` signature standard ``x.509`` for public key certificate.

## smart wallet

this is very cool. wallet is a gateway to decentralized applications on the blockchin. it allow you to hold and secure tokens and other crypot-asset. this blockchin example is built on ``ERC-20`` standards and therefore should be compatible and working out the box with your regular wallet.

## consensus

We've got a fully valid basic Blockchain that accepts transactions and allows us to mine a new block (and get rewarded for it). But the whole point of Blockchains is to be decentralized, and how on earth do we ensure that all the data reflect the same chain? Well, it's actually a well know problem of Consensus, and we are going to have to implement a Consensus Algorithm if we want more that a single node in our network. So better buckle up, we're moving onto registering the new nodes.

## registering new nodes

before you start adding new nodes you'd need to let your node to know about his neighbouring nodes. This needs to be done before you even start implementing Consensus Algorithm. Each node on our network needs to keep registry of other nodes on the network. And therefore we will need to add more endpoints to orchestrate our miner nodes:

- ``/miner/register`` - to register a new miner node into the operation
- ``/miner/nodes/resolve`` - to implement our consensus algorithm to resolve any potential conflicts, making sure all nodes jabe the correct and up to date chain.

```python
...
from urllib.parse import urlpare
...

class Blockchain(object):
  def __init__(self):
    ...
    self.nodes = set()
    ...

  def register_miner_node(self, address):
    # add on the ne miner node into the list of nodes
    parsed_url = urlparse(address)
    self.nodes.add(parse_url.netloc)
    return
```

## implementing the consensus algorithm

as mentioned, conflict is when one node has a different chain to another node. To resolve this, we'll make the rule that the longest valid chain is authoritative. In other words, the longest valid chain is de-facto one. Using this simple rule, we reach Consensuns amongs the nodes in our network.

```python
import request
...
class Blockchain(object):
  ...
  def valid_chain(self, chain):
    # determine if a given blockchain is valid
    last_block = chain[0]
    current_index = 1

    while current_index < len(chain):
      block = chain[current_index]
      # check thaht the hash of the block is correct
      if block['previous_hash'] != self.hash(last_block):
        return False
      # check that proof of work is correct
      if not self.valid_proof(last_block['proof'], block['proof']):
        return False

      last_block = block
      current_index += 1

    return True

  def resolve_conflicts(self):
    # this is our consesu algorithm, it resolve conflicts by replacing
    # our chain with the longlest one in the network.

    neighbours = self.nodes
    new_chain = None

    # we are only looking for the chains longer
    max_length = len(self.chain)

    for node in neighbours:
      response = request.get(f"http://{node}/chain")

      if response.status_code == 200:
        length = response.json()['length']
        chain = response.json()['chain']

        # check if the chain is longer and heter the chains
        # is valid
        if length > max_length and self.valid_chain(chain):
          max_length = length
          new_chain = chain
    
    if new_chain:
      self.chain = new_chain
      return True

    return False
```

so the first method the ``valid_chain`` loops through each block and checks that the chain is valid by verifying both the hash and the proof.

The ``resolve_conflicts`` method loops through all the neighbouring nodes, downloads their chain and verify them using the above ``valid_chain`` method. If a valid chain is found, and it is longer than ours, we replace our chain with this new one.

so, what is left are the very last two API endpoints, specifically one for adding a neighbouring node and another for resolving the conflicts, and it's quite straight forward:

```python
@app.route('/miner/register', method=['POST'])
def register_new_miner():
  values = request.get_json()

  # get the list of miner nodes
  nodes = values.get('nodes')
  if nodes is None:
    return "Error: please supply list of valid nodes", 400

  # register new nodes
  if node in nodes:
    blockchain.register_node(node)

  response = {
    'message': 'our chain was replaced',
    'new_chain': blockchain.chain,
  }
  return jsonify(response), 200

@app.route('/miner/nodes/resolve', method=["POST"])
def consensus():
  # an attempt to resolve conflicts to reach consensus
  conflicts = blockchain.resolve_conflicts()

  if (conflicts):
    response = {
      'message': 'our chain was replace',
      'new_chain': blockhcain.chain,
    }
    return json(response), 200
  response = {
    'message': 'our chain is authoritative',
    'chain': blockchain.chain,
  }
  return jsonify(response), 200
```

And here comes the big one, the one you have been waiting for as at this point you can grab a different machine or a computer if you like and spin up different miners on our network.

or you can run multiple miners on your single machine by running the same process but using a different port number. As an example, I can run another miner node on my machine by running it on a different port and register it with the current miner. Therefore I have two miners: ``http://localhost:5000`` and ``http://localhost:5001``.

## registering new node

```
curl -X POST -H "Content-Type: application/json" -d '{
 "nodes": ["http://127.0.0.1:5001"],
}' "http://localhost:5000/nodes/register"
```

consensus algorithm at work

```
curl http://localhost:5000/nodes/resolve"
```

request ok, returns:


```json
{
    "message": "Our chain was replaced.",
    "new_chain": [
        {
            "index": 1,
            "previous_hash": 1,
            "proof": 100,
            "timestamp": 1525160363.12144,
            "transactions": [],
        },
        {
            "index": 2,
            "previous_hash": "7cd122100c9ded644768ccdec2d9433043968352e37d23526f63eefc65cd89e6",
            "proof": 35293,
            "timestamp": 1525160706.82745,
            "transactions": [
             {
                 "amount": 1,
                 "recipient": "a77f5cdfa2934hv25c7c7da5df1f",
                 "sender": 0,
             }
            ]
        },
    ]
}
```

now go get some friends to mine your Blockchain.