module Block
    extend self

    def difficulty
        3
    end

    def create(index, timestamp, data, prev_hash)
        block = {
            index: index,
            timestamp: timestamp,
            data: data,
            prev_hash: prev_hash,
            difficulty: self.difficulty,
            nonce: ""
        }
        
        block.merge({ hash: self.calculate_hash(block) })
    end

    def calculate_hash(block)
        plain_text = "
            #{block[:index]}
            #{block{:timestamp}}
            #{block[:data]}
            #{block{:prev_hash}}
            #{block[:nonce]}
        "

        sha256 = OpenSSL::Digest.new('SHA256')
        sha256.update(plain_text)
        sha256.to_s
    end

    def is_hash_valid?(hash, difficulty)
        prefix = "0" * difficulty
        hash.starts_with?(prefix)
    end

    def generate(last_block, data)
        new_block = self.create(
            last_block[:index] + 1,
            Time.now.to_s,
            data,
            last_block[:hash]
        )
        i = 0

        loop do
            hex = i.to_s(16)
            new_block = new_block.merge({ nonce: hex })

            if !self.is_hash_valid?(self.calculate_hash(new_block), new_block[:difficulty])
                puts "Mining: try another nonce... #{self.calculate_hash(new_block)}"
                i += 1

                next
            else
                puts "\nComplete mining! nonce for this block is #{new_block[:nonce]}"
                new_block = new_block.merge({ hash: self.calculate_hash(new_block) })
                break
            end
        end

        new_block
    end

    def is_valid?(new_block, old_block)
        if old_block[:index] + 1 != new_block[:index]
            return false
        elsif old_block[:hash] != new_block[:prev_hash]
            return false
        elsif self.calculate_hash(new_block) != new_block[:hash]
            return false
        end

        true
    end
end
