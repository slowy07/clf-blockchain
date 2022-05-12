package clfblockchain;

import java.util.ArrayList;
import com.google.gson.GsonBuilder;

public class ClfBlockchain {
    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static int difficulty = 5;

    public static void main(String[] args) {
        System.out.println("Trying to Mine block 1... ");
		addBlock(new Block("Hi im the first block", "0"));
		
		System.out.println("Trying to Mine block 2... ");
		addBlock(new Block("Yo im the second block",blockchain.get(blockchain.size()-1).hash));
		
		System.out.println("Trying to Mine block 3... ");
		addBlock(new Block("Hey im the third block",blockchain.get(blockchain.size()-1).hash));	
		
		System.out.println("\nBlockchain is Valid: " + isChainValid());
		
		String blockchainJson = StringUtil.getJson(blockchain);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson);
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        
        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);

            previousBlock = blockhain.get(i - 1);

            // cimpaare registered hash and calculated hash
            if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
                System.out.println("previous hashes not equals");
                return false;
            }

            if (!previousBlock.hash.equals(currentBlock.previousHash)) {
                System.out.println("previous hashes not equal");
                return false;
            }
            
            // check if hash solved
            if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
                System.out.println("this block hasn't been mined");
                return false;
            }
        }

        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }
}
