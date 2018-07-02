package it.blockchain.bean;

import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;

import java.util.List;

public class BitcoinTransaction {

    String hash;
    String blockHash;
    List<TransactionInput> txInput;
    List<TransactionOutput> txOutput;

    public BitcoinTransaction(String hash, String blockHash, List<TransactionInput> txInput, List<TransactionOutput> txOutput) {
        this.hash = hash;
        this.blockHash = blockHash;
        this.txInput = txInput;
        this.txOutput = txOutput;
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

    @Override
    public String toString() {

        NetworkParameters params = TestNet3Params.get();
        StringBuilder buf = new StringBuilder("Transaction with hash: " + hash + "\n" +
                "inside block with hash: " + blockHash + "\n");
            for(TransactionInput txi : txInput) {
                if(!txi.isCoinBase()){
                    buf.append("\n from --- " + txi.getFromAddress());
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


        return buf.toString();
    }
}

