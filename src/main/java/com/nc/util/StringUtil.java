package com.nc.util;

import com.nc.Wallet;
import com.nc.tx.Transaction;
import org.bouncycastle.asn1.DERBitString;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Security;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by nitin.yadav on 16-08-2018.
 */
public class StringUtil {

    public static String getStringFromKey(Key key) {

        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static String getDifficultyString(int difficulty) {

        return new String(new char[difficulty]).replace('\0', '0');
    }

    public static String getMerkleRoot(ArrayList<Transaction> transactions) {
        int count = transactions.size();

        List<String> previousTreeLayer = new ArrayList<String>();
        for(Transaction transaction : transactions) {
            previousTreeLayer.add(transaction.transactionId);
        }
        List<String> treeLayer = previousTreeLayer;

        while(count > 1) {
            treeLayer = new ArrayList<String>();
            for(int i=1; i < previousTreeLayer.size(); i+=2) {
                treeLayer.add(CryptoUtil.applySha256(previousTreeLayer.get(i-1) + previousTreeLayer.get(i)));
            }
            count = treeLayer.size();
            previousTreeLayer = treeLayer;
        }

        return (treeLayer.size() == 1) ? treeLayer.get(0) : "";
    }

    public static Key getKeyFromString(String strKey) {

        byte[] encodedKey = Base64.getDecoder().decode(strKey);
        DERBitString derBitString = new DERBitString(encodedKey);

        return new SecretKeySpec(encodedKey,0,encodedKey.length, "ECDSA");
    }

    public static void main(String[] args){

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        Wallet w = new Wallet();
        System.out.println("pub key: " + w.publicKey);
        String pubStr = getStringFromKey(w.publicKey);
        System.out.println("pub key string: " + pubStr);
        System.out.println("restored pub key: " + getKeyFromString(pubStr));
    }

//    public static String getMerkelRootTest(List<String> l){
//
//        int count = l.size();
//        List<String>  prevLayer = new ArrayList<String>();
//        for(String s : l){
//            prevLayer.add(s);
//        }
//        while(count > 1){
//            List<String> newTreeLayer = new ArrayList<String>();
//            for(int i=1 ; i < prevLayer.size(); i+=2){
//                newTreeLayer.add(CryptoUtil.applySha256(prevLayer.get(i-1) + prevLayer.get(i)));
//            }
//            count = newTreeLayer.size();
//            prevLayer = newTreeLayer;
//        }
//        return ()
//    }
}
