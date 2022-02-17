# clf-blockchain

blockchain is an immutable, sequential chain of records called blocks. they can contain tranaction, files or any data you like, really. but the important thing i that they're chained together using hashes.

make sure you have python ``3.6+`` installed (along with ``pip``) and you will also need flask and request library.


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

