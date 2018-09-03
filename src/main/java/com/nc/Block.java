package com.nc;

import com.nc.tx.Transaction;
import com.nc.util.CryptoUtil;
import com.nc.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by nitin.yadav on 16-08-2018.
 */
public class Block {

    public String hash;
    public String prevHash;
    private Long timestamp;
    private int nonce;
    public String merkleRoot;
    public ArrayList<Transaction> transactions = new ArrayList<Transaction>();

    public Block(String prevHash){

        this.prevHash = prevHash;
        this.timestamp = new Date().getTime();
        this.hash = calculateHash();
    }

    public String calculateHash() {

        return CryptoUtil.applySha256(
                prevHash +
                Long.toString(timestamp) +
                Integer.toString(nonce) +
                merkleRoot
        );
    }

    public void mineBlock(int difficulty) {
        System.out.println("#Mining block...");
        merkleRoot = StringUtil.getMerkleRoot(transactions);
        String target = StringUtil.getDifficultyString(difficulty);
        while(!hash.substring( 0, difficulty).equals(target)) {
            nonce ++;
            hash = calculateHash();
        }
        System.out.println("#Total hash generated to mine this block: " + nonce);
        System.out.println("#Block Mined!!!: " + hash);
    }

    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;
        if((!"0".equals(prevHash))) {
            if((!transaction.processTransaction())) {
                System.out.println("#Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("#Transaction Successfully added to Block");
        return true;
    }
}
