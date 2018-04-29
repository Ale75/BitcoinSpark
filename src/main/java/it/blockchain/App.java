package it.blockchain;

import akka.zeromq.Subscribe;
import it.blockchain.utils.PropertiesReader;
import it.blockchain.utils.ZMQConverter;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.zeromq.ZeroMQUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {

    static Logger log = Logger.getLogger(App.class);

    public static void main( String[] args ) throws InterruptedException {

        Map<String,String> propMap = PropertiesReader.readProperties("bitcoin.properties");

        if(log.isDebugEnabled()) {

            log.debug("Contesto caricato");
            log.debug("Host di comunicazione :" + propMap.get("host"));
            log.debug("Connesso a porta:" + propMap.get("port"));
            log.debug("Topic :" + propMap.get("topic"));
        }


        String host = "tcp://"+ propMap.get("host") + ":" + propMap.get("port");
        log.info("Connessione a " +  propMap.get("host"));

        /**
         * Create a local StreamingContext with two working thread and batch interval of 1 second.
         * The master requires 2 cores to prevent a starvation scenario.
         */
        SparkConf sparkConf = new SparkConf().setAppName(propMap.get("appSparkName"));


        // check Spark configuration for master URL, set it to local if not configured
        if (!sparkConf.contains("spark.master")) {
            sparkConf.setMaster("local[2]");
        }

        Subscribe subscribe = new Subscribe(propMap.get("topic"));

        Function<byte[][], Iterable<String>> bytesToObjects = new Function<byte[][], Iterable<String>>() {
            @Override
            public Iterable<String> call(byte[][] bytes) throws Exception {
                String[] pay = new String[bytes.length];

                for(int i = 0; i < bytes.length; i++) {
                    pay[i] = ZMQConverter.bin2hex(bytes[i]);
                    log.info(pay[i]);
                }
                return Arrays.asList(pay);
            }
        };

        // Create the context and set the batch size
        /**
         *  StreamingContext is the main entry point for all streaming functionality.
         *  We create a local StreamingContext with two execution threads,
         *  and a batch interval of 1 second.
         *  https://spark.apache.org/docs/latest/streaming-programming-guide.html
         */
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, new Duration(2000));


        JavaDStream<String> lines = ZeroMQUtils.createStream(streamingContext, host, subscribe
                , bytesToObjects );

        /*JavaDStream<String> objectJavaDStream = lines.flatMap(new FlatMapFunction<String, String>() {
              @Override
              public Iterator<String> call(String s) {
                  log.info(s);
                  return Arrays.asList(s.split(" ")).iterator();
              }
        });*/

        lines.print();

        streamingContext.start();
        streamingContext.awaitTermination();

    }

}
