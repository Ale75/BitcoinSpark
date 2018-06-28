package it.blockchain.utils;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;

import java.util.List;

public class BlockTestNetManager {



    public BlockTestNetManager() {
    }

    /**
     * Create a block object from bytes
     * @param bytes array of bytes
     * @return Block object
     */
    public Block blockMakerFromBytes(List<byte[]> bytes){

        Block block = null;
        NetworkParameters params = TestNet3Params.get();
        Context context = new Context(params);
        BitcoinSerializer bt = new BitcoinSerializer(params,true);
        block = bt.makeBlock(bytes.get(0));
        //block = new Block(params, bytes, context.getParams().getDefaultSerializer(), bytes.length); //Deprecated
        return block;
    }




}
