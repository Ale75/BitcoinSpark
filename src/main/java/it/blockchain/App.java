package it.blockchain;

import akka.zeromq.Subscribe;
import it.blockchain.utils.PropertiesReader;
import org.apache.commons.codec.binary.Hex;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.zeromq.ZeroMQUtils;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;

import java.util.*;

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

        Function<byte[][], Iterable<byte[]>> bytesToObjects = new Function<byte[][], Iterable<byte[]>>() {
            @Override
            public Iterable<byte[]> call(byte[][] bytes) throws Exception {


               /* BitcoinBlockReader bbr = null;
                boolean direct=false;

                String exampleString = new String(bytes[0],StandardCharsets.UTF_8);
                InputStream targetStream = new ByteArrayInputStream(exampleString.getBytes(StandardCharsets.UTF_8));
                bbr = new BitcoinBlockReader(targetStream, Constants.DEFAULT_MAXSIZE_BITCOINBLOCK, Constants.DEFAULT_BUFFERSIZE, Constants.TESTNET3_MAGIC, direct);
                //BitcoinBlock block = bbr.readBlock();
                ByteBuffer genesisByteBuffer = bbr.readRawBlock();


                String[] pay = new String[bytes.length];

                for(int i = 0; i < bytes.length; i++) {
                    pay[i] = ZMQConverter.bin2hex(bytes[i]);
                    log.info(pay[i]);
                }*/

                //log.info(Hex.encodeHexString(bytes[0]));

                /*NetworkParameters params = TestNet3Params.get();
                Context context = new Context(params);

                Block block = new Block(params, bytes[0], context.getParams().getDefaultSerializer(), bytes[0].length);
                log.info(block.getHashAsString());*/

                /*Map<Block,byte[]> listOfBlocks = new HashMap<Block,byte[]>();
                listOfBlocks.put(block,bytes[0]);*/

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
         * Save raw data to hdfs
         */
        if(Boolean.parseBoolean(propMap.get("enableHDFS"))) {
            lines.foreachRDD((v1, v2) -> {
                if (v1.rdd().count() > 0)
                    v1.rdd().saveAsTextFile(hadoopHdfs + "/bitcoin/raw/" + v2.toString().split(" ")[0]);
            });
        }

        /**
         * Read raw array bytes and generate a Block object
         */
        JavaDStream<Block> blocks = lines.map(bytes -> {

            Block block = null;

            if(bytes.length > 0) {
                NetworkParameters params = TestNet3Params.get();
                Context context = new Context(params);
                BitcoinSerializer bt = new BitcoinSerializer(params,true);
                block = bt.makeBlock(bytes);
                //block = new Block(params, bytes, context.getParams().getDefaultSerializer(), bytes.length); //Deprecated
            }

            return block;
        });


        lines.print();
        blocks.print();


        streamingContext.start();
        streamingContext.awaitTermination();

    }

}
