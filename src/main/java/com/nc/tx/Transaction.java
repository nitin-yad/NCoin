package com.nc.tx;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.nc.NCChain;
import com.nc.util.CryptoUtil;
import com.nc.util.StringUtil;

import java.lang.reflect.Type;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by nitin.yadav on 16-08-2018.
 */
public class Transaction {

    public String transactionId;
    public PublicKey sender;
    public PublicKey receiver;
    public float amount;
    public byte[] signature;

    public List<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public List<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    private static long sequence = 0;

    public Transaction(PublicKey sender, PublicKey receiver, float amount, List<TransactionInput> inputs){

        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.inputs = inputs;
    }


    private String calculateHash(){

        sequence++;
        return CryptoUtil.applySha256(StringUtil.getStringFromKey(sender)+
                        StringUtil.getStringFromKey(receiver)+
                        Float.toString(amount) + sequence);
    }

    public void generateSignature(PrivateKey privateKey) {

        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receiver) + Float.toString(amount)	;
        signature = CryptoUtil.applyECDSASig(privateKey,data);
    }

    public boolean verifySignature() {

        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receiver) + Float.toString(amount)	;
        return CryptoUtil.verifyECDSASig(sender, data, signature);
    }


    public boolean processTransaction() {

        if(!verifySignature()) {
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }

        //Gathers transaction inputs (Making sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = NCChain.UTXOs.get(i.transactionOutputId);
        }

        //Checks if transaction is valid:
        if(getInputsValue() < NCChain.minTransAmount) {
            System.out.println("#Transaction Inputs too small: " + getInputsValue());
            System.out.println("#Please enter the amount greater than " + NCChain.minTransAmount);
            return false;
        }

        //Generate transaction outputs:
        float leftOver = getInputsValue() - amount; //get value of inputs then the left over change:
        transactionId = calculateHash();
        outputs.add(new TransactionOutput( this.receiver, amount,transactionId)); //send value to receiver
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender

        //Add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            NCChain.UTXOs.put(o.id , o);
        }

        //Remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            NCChain.UTXOs.remove(i.UTXO.id);
        }

        return true;
    }

    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }


    public static JsonSerializer<Transaction> serializer = new JsonSerializer<Transaction>() {

        public JsonElement serialize(Transaction src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject transaction = new JsonObject();

            transaction.addProperty("transactionId", src.transactionId);
            transaction.addProperty("sender", StringUtil.getStringFromKey(src.sender));
            transaction.addProperty("receiver", StringUtil.getStringFromKey(src.receiver));
            transaction.addProperty("signature", Arrays.toString(src.signature));
            return transaction;
        }
    };

}
