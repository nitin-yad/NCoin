package com.nc.tx;

import com.nc.util.CryptoUtil;
import com.nc.util.StringUtil;

import java.security.PublicKey;

/**
 * Created by nitin.yadav on 16-08-2018.
 */
public class TransactionOutput {

    public String id;
    public PublicKey receiver; //also known as the new owner of these coins.
    public float value; //the amount of coins they own
    public String parentTransactionId; //the id of the transaction this output was created in

    //Constructor
    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.receiver = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = CryptoUtil.applySha256(StringUtil.getStringFromKey(reciepient)+Float.toString(value)+parentTransactionId);
    }

    //Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == receiver);
    }
}
