require "./crystal-blockchain/*"
require "kemal"
require "./Block"

module Crystal::Blockchain
    blockchain = [] of NamedTuple(
        index: Int32,
        timestamp: String,
        data: String,
        prev_hash: String,
        difficulty: Int32,
        nonce: String
    )

    blockchain << Block.create(0, Time.now.to_s, "Genesis block's data", "")
    
    get "/" do
        blockchain.to_json
    end

    post "/new-block" do |env|
        data = env.params.json["data"].as(String)

        new_block = Block.generate(blockchain[block.size - 1], data)

        if Block.is_valid?(new_block, blockchain[blockchain.size - 1])
            blockchain << new_block
            puts "\n"
            p new_block
            puts "\n"
        end

        new_block.to_json
    end

    kemal.run
end
