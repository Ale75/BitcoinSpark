package it.blockchain.bean;

import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BitcoinTransaction {

    String hash;
    String blockHash;
    List<TransactionInput> txInput;
    List<TransactionOutput> txOutput;
    int transactionIndex;
    Date receivedTime;


    public BitcoinTransaction(String hash, String blockHash, List<TransactionInput> txInput, List<TransactionOutput> txOutput, int transactionIndex, Date receivedTime) {
        this.hash = hash;
        this.blockHash = blockHash;
        this.txInput = txInput;
        this.txOutput = txOutput;
        this.transactionIndex = transactionIndex;
        this.receivedTime = receivedTime;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(int transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getHash() {
        return hash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public List<TransactionInput> getTxInput() {
        return txInput;
    }

    public List<TransactionOutput> getTxOutput() {
        return txOutput;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public void setTxInput(List<TransactionInput> txInput) {
        this.txInput = txInput;
    }

    public void setTxOutput(List<TransactionOutput> txOutput) {
        this.txOutput = txOutput;
    }


    public Date getReceivedTime() {
        return receivedTime;
    }

    public void setReceivedTime(Date receivedTime) {
        this.receivedTime = receivedTime;
    }

    public List<String> getValidSender(){

        List<String> bitcoinSender = new ArrayList<String>();

        for(TransactionInput txi : this.txInput) {
            if(!txi.isCoinBase()) {
                try {
                    bitcoinSender.add(txi.getFromAddress().toString());
                } catch (ScriptException exS){
                    bitcoinSender.add("UNKNOWN");
                }
            } else {
                bitcoinSender.add("COIN_BASE");
            }
        }
        return bitcoinSender;
    };

    public List<TransactionDBOutput> getValidReceiver() {

        List<TransactionDBOutput> bitcoinReceiver = new ArrayList<TransactionDBOutput>();
        NetworkParameters params = TestNet3Params.get();

        for (TransactionOutput txout : this.txOutput) {
            Address txToHash = (txout.getAddressFromP2PKHScript(params) != null) ? txout.getAddressFromP2PKHScript(params)
                    : txout.getAddressFromP2SH(params);
            Coin value = txout.getValue();

            if (txToHash != null) {
                TransactionDBOutput tDBOut = new TransactionDBOutput(txToHash.toString(), Double.valueOf(value.toPlainString()));
                bitcoinReceiver.add(tDBOut);
            }

        }

        return bitcoinReceiver;

    }

    @Override
    public String toString() {

        NetworkParameters params = TestNet3Params.get();
        StringBuilder buf = new StringBuilder("Transaction with hash: " + hash + "\n" +
                "inside block with hash: " + blockHash + "\n" +
                " block number: " + transactionIndex  + "\n");

            for(TransactionInput txi : txInput) {
                if(!txi.isCoinBase()) {
                    try {

                        buf.append("\n from --- " + txi.getFromAddress());

                    } catch (ScriptException exSc){
                        buf.append("\n from UNKNOWN ---");
                    }
                } else {
                    buf.append("\n COIN BASE --- ");
                }
            }

            for(TransactionOutput txout : txOutput){
                Address txToHash = (txout.getAddressFromP2PKHScript(params) != null) ? txout.getAddressFromP2PKHScript(params)
                        : txout.getAddressFromP2SH(params);
                Coin value = txout.getValue();

                if (txToHash != null)
                    buf.append("\n to --> " + txToHash.toString() + " with value ----- > " + value.toPlainString());
            }

            buf.append("\n received time --> " + this.receivedTime);

        return buf.toString();
    }
}

