package com.nc;

import com.nc.tx.Transaction;
import com.nc.tx.TransactionInput;
import com.nc.tx.TransactionOutput;
import com.nc.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by nitin.yadav on 16-08-2018.
 */
public class NCChain {

    public static List<Block> bChain = new ArrayList<Block>();
    public static Map<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public static int difficulty = 4;
    public static float minTransAmount = 0.1f;

    public static Transaction genesisTransaction;
    public static Block genesisBlock;

    public static Wallet coinbase;

    static{

        coinbase = new Wallet();

        //create genesis transaction, which sends 100 NoobCoin to walletA:
        genesisTransaction = new Transaction(coinbase.publicKey, coinbase.publicKey, 100f, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.amount, genesisTransaction.transactionId)); //manually add the Transactions Output
        UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); //its important to store our first transaction in the UTXOs list.

        System.out.println("#Creating and Mining Genesis block... ");
        genesisBlock = new Block("0");
        genesisBlock.addTransaction(genesisTransaction);
        addBlock(genesisBlock);
    }

    public static Boolean isValidChain(){

        Block currBlock, prevBlock;
        String hashTarget = StringUtil.getDifficultyString(difficulty);
        Map<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        for(int i = 1; i < bChain.size(); i++){

            currBlock =  bChain.get(i);
            prevBlock = bChain.get(i-1);

            if(!currBlock.hash.equals(currBlock.calculateHash())){

                System.out.println("#Current hashes are not equal");
                return false;
            }
            if(!prevBlock.hash.equals(currBlock.prevHash)){

                System.out.println("#Previous hashes are not equal");
            }
            if(!currBlock.hash.substring(0, difficulty).equals(hashTarget)){

                System.out.println("#This block has not been mined");
            }
            for(int t =0; t < currBlock.transactions.size(); t++){

                Transaction currTrans = currBlock.transactions.get(t);

                if(!currTrans.verifySignature()){

                    System.out.println("#Signature on transaction " + t +" is not valid");
                    return false;
                }
                if(currTrans.getInputsValue() != currTrans.getOutputsValue()){

                    System.out.println("#Inputs are not equal to outputs on transation "+ t) ;
                    return false;
                }
                TransactionOutput tempOutput;
                for(TransactionInput input: currTrans.inputs){

                    tempOutput = tempUTXOs.get(input.transactionOutputId);
                    if(tempOutput == null){

                        System.out.println("#Referenced input on transaction "+ t +" is missing");
                        return false;
                    }
                    if(input.UTXO.value != tempOutput.value){

                        System.out.println("#Referenced input on transaction "+ t +" value is invalid");
                        return false;
                    }
                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currTrans.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currTrans.outputs.get(0).receiver != currTrans.receiver) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if( currTrans.outputs.get(1).receiver != currTrans.sender) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }
            }

        }
        System.out.println("#Blockshain is valid");
        return true;
    }

    public static void addBlock(Block block){

        block.mineBlock(difficulty);
        bChain.add(block);
    }
}
