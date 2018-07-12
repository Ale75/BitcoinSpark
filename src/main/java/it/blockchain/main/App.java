package it.blockchain.main;

import akka.zeromq.Subscribe;
import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import it.blockchain.utils.KafkaProducerBuilder;
import it.blockchain.bean.BitcoinTransaction;
import it.blockchain.bean.TransactionDBWrapper;
import it.blockchain.utils.BlockTestNetManager;
import it.blockchain.utils.PropertiesReader;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.zeromq.ZeroMQUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Hello world!
 *
 */
public class App {

    static Logger log = Logger.getLogger(App.class);

    public static void main( String[] args ) throws InterruptedException {

        Map<String,String> propMap = PropertiesReader.readProperties("bitcoin.properties");


        String host = "tcp://"+ propMap.get("sparkHost") + ":" + propMap.get("sparkPort");
        String hadoopHdfs = propMap.get("hadoopHDFS");
        String mongoHost = propMap.get("mongoDBHost");
        String mongoDBPort = propMap.get("mongoDBPort");
        String mongoDB = propMap.get("mongoDB");
        String mongoDBCollection = propMap.get("mongoCollection");

        String kafkaHost = propMap.get("kafkaHost");
        String kafkaPort = propMap.get("kafkaPort");
        String kafkaTopic = propMap.get("kafkaTopic");
        String kafkaAppID = propMap.get("kafkaAppID");

        if(log.isDebugEnabled()) {

            log.debug("Contesto caricato");
            log.debug("Host di comunicazione :" + propMap.get("sparkHost"));
            log.debug("Connesso a porta:" + propMap.get("sparkPort"));
            log.debug("Topic :" + propMap.get("topic"));
        }



        log.info("Connessione a " +  propMap.get("sparkHost"));
        log.info("HadoopFS url: " + hadoopHdfs);
        log.info("MongoDB host: " + mongoHost);
        log.info("MongoDB : " + mongoDB);
        log.info("MongoDB Collection: " + mongoDBCollection);
        log.info("MongoDB Port: " + mongoDBPort);
        log.info("KafkaHost: " + kafkaHost);
        log.info("KafkaHost Port: " + kafkaPort);
        log.info("Kafka Topic: " + kafkaTopic);
        log.info("Kafka AppID: " + kafkaAppID);



        /**
         *
         * Grafo delle transazioni
         *
         */
        //Graph<Transaction,String> transactionsGraph;


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
         * Read raw array bytes and generate a Block object saved into MongoDB
         */

        lines.foreachRDD(new VoidFunction2<JavaRDD<byte[]>, Time>() {


            Mongo mongo = new Mongo(mongoHost, Integer.parseInt(mongoDBPort));
            DB db = mongo.getDB(mongoDB);
            DBCollection collection = db.getCollection(mongoDBCollection);

            @Override
            public void call(JavaRDD<byte[]> bytes, Time time) throws Exception {

                List<byte[]> blockAsByte = bytes.collect();
                NetworkParameters params = TestNet3Params.get();
                Gson gson = new Gson();

                if (!blockAsByte.isEmpty()) {
                    BlockTestNetManager blockManager = new BlockTestNetManager();
                    Block block = blockManager.blockMakerFromBytes(blockAsByte);
                    log.info("####### NEW BLOCK with hash: " + block.getHashAsString() + " ###########");
                    log.info("Transaction into block: " + block.getTransactions().size());

                    for (int i = 0; i < block.getTransactions().size(); i++) {
                        Transaction tx = block.getTransactions().get(i);
                        BitcoinTransaction bTx = new BitcoinTransaction(tx.getHashAsString(),
                                block.getHashAsString(), tx.getInputs(), tx.getOutputs(), i);
                        TransactionDBWrapper txDB = new TransactionDBWrapper(tx.getHashAsString(), block.getHashAsString(),
                                bTx.getValidSender(), bTx.getValidReceiver());
                        String json = gson.toJson(txDB);
                        DBObject dbObject = (DBObject) JSON.parse(json);
                        collection.insert(dbObject);
                        log.info("Saved into db: " + json);

                        final Producer<Long, String> producer = KafkaProducerBuilder.createProducer(kafkaHost + ":" + kafkaPort, kafkaAppID );
                        final ProducerRecord<Long, String> record =
                                new ProducerRecord<>(kafkaTopic, new Random().nextLong(),
                                        json);
                        try {
                            RecordMetadata metadata = producer.send(record).get();
                        } finally {
                            producer.flush();
                            producer.close();
                        }

                    }

                    log.info("End transaction");
                }
            }
        });


        lines.print();

        streamingContext.start();
        streamingContext.awaitTermination();

    }


}
