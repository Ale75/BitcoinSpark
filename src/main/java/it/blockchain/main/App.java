package it.blockchain.main;

import akka.zeromq.Subscribe;
import com.google.gson.Gson;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import it.blockchain.bean.TransactionDBOutput;
import it.blockchain.neo4j.Neo4jManager;
import it.blockchain.utils.*;
import it.blockchain.bean.BitcoinTransaction;
import it.blockchain.bean.TransactionDBWrapper;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.rdd.RDD;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.zeromq.ZeroMQUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet3Params;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.spark.Neo4j;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
        String neo4jHost = propMap.get("neo4jHost");
        String neo4jPort = propMap.get("neo4jPort");
        String neo4jConnectionUrl = "bolt://" + neo4jHost + ":" + neo4jPort;

        Boolean enableMongo = Boolean.parseBoolean(propMap.get("enableMongo"));


        log.info("Contesto caricato");
        log.info("Host di comunicazione :" + propMap.get("sparkHost"));
        log.info("Connesso a porta:" + propMap.get("sparkPort"));
        log.info("ZMQ Topic :" + propMap.get("topic"));
        log.info("Connessione a " +  propMap.get("sparkHost"));
        log.info("HadoopFS url: " + hadoopHdfs);
        log.info("Mongo is enabled: " + Boolean.toString(enableMongo));
        log.info("MongoDB host: " + mongoHost);
        log.info("MongoDB : " + mongoDB);
        log.info("MongoDB Collection: " + mongoDBCollection);
        log.info("MongoDB Port: " + mongoDBPort);
        log.info("KafkaHost: " + kafkaHost);
        log.info("KafkaHost Port: " + kafkaPort);
        log.info("Kafka Topic: " + kafkaTopic);
        log.info("Kafka AppID: " + kafkaAppID);
        log.info("Neo4j host: " + neo4jHost);
        log.info("Neo4j port: " + neo4jPort);
        log.info("Neo4j Connection url: " + neo4jConnectionUrl);


        /**
         * Create a local StreamingContext with two working thread and batch interval of 1 second.
         * The master requires 2 cores to prevent a starvation scenario.
         */
        SparkConf sparkConf = new SparkConf().setAppName(propMap.get("appSparkName"));
        Subscribe subscribe = new Subscribe(propMap.get("topic"));

        // check Spark configuration for master URL, set it to local if not configured
        if (!sparkConf.contains("spark.master")) {
            sparkConf.setMaster("local[2]"); //local[K] (Run Spark locally with K threads, usually k is set up to match the number of cores on your machine)
        }


        Function<byte[][], Iterable<byte[]>> bytesToObjects = new Function<byte[][], Iterable<byte[]>>() {
            @Override
            public Iterable<byte[]> call(byte[][] bytes) throws Exception {
                Iterable iterable = Arrays.asList(bytes[0]);
                return iterable;
            }
        };

        /**
         * Configuration of Neo4j
         * */
        sparkConf.set("spark.neo4j.bolt.url", neo4jConnectionUrl);
        Neo4jManager neo4jManager = new Neo4jManager(neo4jConnectionUrl);

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
         * Neo4j Spark Connector Object
         * From doc new Neo4j(SparkContext)
         */
        Neo4j neo4jSparkConnector = new Neo4j(streamingContext.sparkContext().sc());
        HashMap<String,Object> mapParams = new HashMap<String,Object>();
        RDD nodes = neo4jSparkConnector.cypher("Match (n) return n;", ScalaUtils.toScalaMap(mapParams)).loadRowRdd();
        log.info("Totale nodi attualmente nel database: " + nodes.count());




        /**
         * Save to HDFS
         * */
        if(Boolean.parseBoolean(propMap.get("enableHDFS"))){
            lines.foreachRDD((bytes, time)-> {

                List<byte[]> blockAsByte = bytes.collect();
                if (!blockAsByte.isEmpty()) {
                    bytes.coalesce(1).saveAsTextFile(hadoopHdfs + File.separator + "blocks" + File.separator);
                }

            });
        }

        /**
         * Read raw array bytes and generate a Block object saved into MongoDB
         */

        lines.foreachRDD(new VoidFunction2<JavaRDD<byte[]>, Time>() {


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
                                block.getHashAsString(), tx.getInputs(), tx.getOutputs(),
                                i, new Date());


                        TransactionDBWrapper txJSON = new TransactionDBWrapper(tx.getHashAsString(), block.getHashAsString(),
                                bTx.getValidSender(), bTx.getValidReceiver(), bTx.getReceivedTime());
                        String json = gson.toJson(txJSON);

                        if(enableMongo){
                            log.info("#### Saving data to MongoDB ####");
                            Mongo mongo = new Mongo(mongoHost, Integer.parseInt(mongoDBPort));
                            DB db = mongo.getDB(mongoDB);
                            DBCollection collection = db.getCollection(mongoDBCollection);
                            DBObject dbObject = (DBObject) JSON.parse(json);
                            collection.insert(dbObject);
                            log.info("Saved into MongoDB: " + json);
                        }

                        DateFormat df = new SimpleDateFormat(Constants.DATE_FORMATTER);
                        for(TransactionDBOutput tOut : bTx.getValidReceiver()) {

                            log.info("#### Saving transaction in Neo4j ####");
                            neo4jManager.createORupdate(Constants.NODE_LABEL, String.join(Constants.STRING_DELIMITER, bTx.getValidSender()), Constants.NODE_LABEL, tOut.getHash(),
                                    Constants.RELATIONS_LABEL, Constants.TYPE_OF_MONEY, Double.toString(tOut.getValue()),bTx.getHash() ,bTx.getBlockHash(), df.format(bTx.getReceivedTime()));
                            /*neo4jManager.createRelation(Constants.NODE_NAME_FROM, Constants.NODE_LABEL, String.join(Constants.STRING_DELIMITER, bTx.getValidSender()), Constants.RELATIONS_LABEL,
                                    Constants.TYPE_OF_MONEY, Double.toString(tOut.getValue()) ,
                                    bTx.getHash() ,  bTx.getBlockHash(), df.format(bTx.getReceivedTime()),
                                    Constants.NODE_NAME_TO, Constants.NODE_LABEL, tOut.getHash());*/
                        }

                        log.info("#### Sending data to kafka ####");
                        final Producer<Long, String> producer = KafkaProducerBuilder.createProducer(kafkaHost + ":" + kafkaPort, kafkaAppID );
                        try {
                            log.info("Sending JSON: " + json);
                            final ProducerRecord<Long, String> record =
                                    new ProducerRecord<>(kafkaTopic, new Random().nextLong(),
                                            json);
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
