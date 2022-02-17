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

