package it.blockchain.neo4j;


import it.blockchain.utils.Constants;
import org.neo4j.driver.v1.*;
import org.apache.log4j.Logger;
import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jManager implements AutoCloseable {

    static Logger log = Logger.getLogger(Neo4jManager.class);
    private Driver driver;

    public Neo4jManager(String neo4jConnectionUrl) {
       driver =  GraphDatabase.driver(neo4jConnectionUrl); //AuthTokens.basic( user, password ) ); Set if Auth is enable
    }


    public void createRelation(String $fromNodeName, String $fromLabel, String $hashFrom, String $relationLabel, String $relType,
                                    String $relValue, String $transactionHash, String $blockHash, String receivedDate,
                                String $toNodeName, String $toLabel, String $hashTo){

        try (Session session = driver.session()) {
            String greeting = session.writeTransaction(new TransactionWork<String>(){

                    @Override
                    public String execute( Transaction tx ) {
                        StatementResult result = tx.run(
                                "Merge (" + $fromNodeName + ":" + $fromLabel + " {hash:'" + $hashFrom + "'})" +
                                        "-[r:" + $relationLabel + " {type:'" + $relType + "', value: '" + $relValue +
                                        "', transactionHash:'" + $transactionHash +"',blockHash: '" + $blockHash + "', receivedTime:'" + receivedDate + "'}]->" +
                                        "(" + $toNodeName + ":" + $toLabel +" {hash:'" + $hashTo + "'})"
                                ,
                                parameters( "","") );
                        return "Relationship Saved";
                    }

            });

            log.info(greeting);
        }
    }


    public void createORupdate(String $fromLabel, String $hashFrom, String $toLabel, String $hashTo, String $relationLabel, String $relType,
                               String $relValue, String $transactionHash, String $blockHash, String receivedDate){

        try (Session session = driver.session()) {
            String greeting = session.writeTransaction(new TransactionWork<String>(){

                @Override
                public String execute( Transaction tx ) {
                    StatementResult result = tx.run("Merge (a:" + $fromLabel + "{hash:'"+ $hashFrom + "'})\n" +
                                    "Merge (b:" + $toLabel + "{hash:'" + $hashTo + "'})\n" +
                                    "Merge (a)-[r:" + $relationLabel + " {type: '" + $relType + "', value: '" + $relValue +
                            "', transactionHash:'" + $transactionHash +"',blockHash: '" + $blockHash + "', receivedTime:'" + receivedDate + "'}]->(b);",

                            parameters( "","") );
                    return "Relationship Saved";
                }

                });
        }
    }

    public void createStaticNode(){

        try (Session session = driver.session()) {
            String greeting = session.writeTransaction(new TransactionWork<String>(){

                @Override
                public String execute( Transaction tx ) {
                    StatementResult result = tx.run(
                            "Create (a: {hash:'$nodeHash'})" ,
                            parameters( "", "") );
                    return  null;
                }

                });
        }
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }
}
