package it.blockchain.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertiesReader {



    public PropertiesReader() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, String> readProperties(String propFileName){

        Map<String, String>  propMap = new HashMap<String,String>();
        Properties prop = new Properties();

        try {

            prop.load(PropertiesReader.class.getClassLoader().getResourceAsStream(propFileName));

            for (String key : prop.stringPropertyNames()) {
                String value = prop.getProperty(key);
                propMap.put(key, value);
            }

        } catch (IOException e) {

            e.printStackTrace();

        }

        return propMap;
    }
}
