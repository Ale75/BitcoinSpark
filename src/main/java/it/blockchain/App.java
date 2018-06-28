package it.blockchain;

import akka.zeromq.Subscribe;
import it.blockchain.utils.BlockTestNetManager;
import it.blockchain.utils.PropertiesReader;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.graphx.Graph;
import org.apache.spark.graphx.VertexRDD;
import org.apache.spark.rdd.RDD;
import org.apache.spark.storage.BlockManager;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.zeromq.ZeroMQUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
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
        String hadoopHdfs = propMap.get("hadoopHDFS");
        log.info("HadoopFS url: " + hadoopHdfs);

        /**
         *
         * Grafo delle transazioni
         *
         */
        Graph<Transaction,String> transactionsGraph;


        /**
         * Create a local StreamingContext with two working thread and batch interval of 1 second.
         * The master requires 2 cores to prevent a starvation scenario.
         */
        SparkConf sparkConf = new SparkConf().setAppName(propMap.get("appSparkName"));


        // check Spark configuration for master URL, set it to local if not configured
        if (!sparkConf.contains("spark.master")) {
            sparkConf.setMaster("local[2]"); //local[K] (Run Spark locally with K threads, usually k is set up to match the number of cores on your machine)
        }

        Subscribe subscribe = new Subscribe(propMap.get("topic"));

        Function<byte[][], Iterable<byte[]>> bytesToObjects = new Function<byte[][], Iterable<byte[]>>() {
            @Override
            public Iterable<byte[]> call(byte[][] bytes) throws Exception {
                Iterable iterable = Arrays.asList(bytes[0]);
                return iterable;
            }
        };

        // Create the context and set the batch size
        /**
         *  StreamingContext is the main entry point for all streaming functionality.
         *  We create a local StreamingContext with two execution threads,
         *  and a batch interval of 2 seconds.
         *  https://spark.apache.org/docs/latest/streaming-programming-guide.html
         */
        JavaStreamingContext streamingContext = new JavaStreamingContext(sparkConf, new Duration(2000));


        /**
         * Get data from ZMQ publisher and return an array of bytes
         */
        JavaDStream<byte[]> lines = ZeroMQUtils.createStream(streamingContext, host, subscribe
                , bytesToObjects );


        /**
         * Read raw array bytes and generate a Block object
         */
        lines.foreachRDD( (bytes, time) -> {

            List<byte[]> blockAsByte =  bytes.collect();

           if(!blockAsByte.isEmpty()){
               BlockTestNetManager blockManager = new BlockTestNetManager();
               Block block = blockManager.blockMakerFromBytes(blockAsByte);
               log.info("####### NEW BLOCK with hash: " + block.getHashAsString() + " ###########");

           }

        });

        lines.print();

        streamingContext.start();
        streamingContext.awaitTermination();

    }


}
