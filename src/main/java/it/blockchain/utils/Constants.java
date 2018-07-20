package it.blockchain.utils;

import java.util.HashMap;

public class Constants {

    public static final int DEFAULT_BUFFERSIZE=64*1024;
    public static final int DEFAULT_MAXSIZE_BITCOINBLOCK=8 * 1024 * 1024;
    public static final byte[][] DEFAULT_MAGIC = {{(byte)0xF9,(byte)0xBE,(byte)0xB4,(byte)0xD9}};
    public static final byte[][] TESTNET3_MAGIC = {{(byte)0x0B,(byte)0x11,(byte)0x09,(byte)0x07}};
    public static final byte[][] TESTNET_MAGIC = {{(byte)0x0B,(byte)0xBF,(byte)0xB5,(byte)0xDA}};
    public static final byte[][] MULTINET_MAGIC = {{(byte)0xF9,(byte)0xBE,(byte)0xB4,(byte)0xD9},{(byte)0x0B,(byte)0x11,(byte)0x09,(byte)0x07}};


    public static final String NODE_NAME_FROM = "node_from";
    public static final String NODE_NAME_TO = "node_to";
    public static final String NODE_LABEL = "hash_pub";
    public static final String RELATIONS_LABEL = "send";

    public static final String TYPE_OF_MONEY = "BTC";
    public static final String DATE_FORMATTER = "dd/MM/yyyy HH:mm:ss";
    public static final String STRING_DELIMITER = "-";
}
