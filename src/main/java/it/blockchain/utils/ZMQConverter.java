package it.blockchain.utils;

import java.util.Formatter;

public class ZMQConverter {

    /**
     * https://bitcoin.stackexchange.com/questions/70063/how-do-i-parse-the-zeromq-messages-in-java
     */

    private ZMQConverter() {
        throw new IllegalStateException("Utility class");
    }

    public static String bin2hex(byte[] bytes)
    {
        Formatter f = new Formatter();
        try {
            for (byte c : bytes)
                f.format("%02X",c);

            return (f.toString().toLowerCase());
        } finally {
            f.close();
        }
    }

    public static String hex2bin(String hexString)
    {
        if (!hexString.matches("^[0-9a-fA-F]+$")) {
            return null;
        }
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hexString.length(); i+=2) {
            String str = hexString.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        return output.toString();
    }
}