package com.nc.test;

import com.google.gson.GsonBuilder;
import com.nc.Block;
import com.nc.NCChain;
import com.nc.Wallet;
import com.nc.tx.Transaction;

import java.security.Security;

/**
 * Created by nitin.yadav on 16-08-2018.
 */
public class Test {

    public static Wallet walletA;
    public static Wallet walletB;

    public static void main(String[] args){

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //Create wallets:
        walletA = new Wallet();
        walletB = new Wallet();

        //testing
        Block block1 = new Block(NCChain.genesisBlock.hash);
        System.out.println("#Balance in coinbase: " + NCChain.coinbase.getBalance());
        System.out.println("#WalletA's balance is: " + walletA.getBalance());
        System.out.println("#Coinbase is Attempting to send funds (40) to WalletA...");
        block1.addTransaction(NCChain.coinbase.sendFunds(walletA.publicKey, 40f));
        NCChain.addBlock(block1);
        System.out.println("#Coinbase's balance is: " + NCChain.coinbase.getBalance());
        System.out.println("#WalletA's balance is: " + walletA.getBalance());

        Block block2 = new Block(block1.hash);
        System.out.println("#WalletA Attempting to send more funds (1000) than it has...");
        block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
        NCChain.addBlock(block2);
        System.out.println("#WalletA's balance is: " + walletA.getBalance());
        System.out.println("#WalletB's balance is: " + walletB.getBalance());

        Block block3 = new Block(block2.hash);
        System.out.println("#WalletB is Attempting to send funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
        NCChain.addBlock(block3);
        System.out.println("#WalletA's balance is: " + walletA.getBalance());
        System.out.println("#WalletB's balance is: " + walletB.getBalance());

        System.out.println("#Blockchain valid? " + NCChain.isValidChain());

        String blockchainJson = new GsonBuilder().registerTypeAdapter(Transaction.class, Transaction.serializer).setPrettyPrinting().create().toJson(NCChain.bChain);
        System.out.println("#The block chain: ");
        System.out.println(blockchainJson);
    }
}
