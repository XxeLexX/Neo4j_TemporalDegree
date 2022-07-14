package labor.toys;

import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class StringToCypher {
    GraphDatabaseService graphDb;

    Node tempNode;

    // Consider use the DegreeType as the relationship,
    // since it can be used as the param for the method getDegree(RelationshipType relationshipType)
    Relationship relationship;

    public void convert(String csvString, String dbPath){
        // Handel the String
        String[] temp = csvString.split(";");
        String[] timeString = temp[temp.length-1].split("\\),\\(");

        String valid_time_string = timeString[0];
        String transaction_time_string = timeString[1];

        // get the time via pattern and matcher
        Pattern pattern = Pattern.compile("-?\\d+");

        Matcher valid_matcher = pattern.matcher(valid_time_string);
        Matcher tx_machter = pattern.matcher(transaction_time_string);

        // try to set as valid and tx in Node
        ArrayList<String> v_timeArray = new ArrayList<>();
        ArrayList<String> tx_timeArray = new ArrayList<>();

        while(valid_matcher.find()){
            v_timeArray.add(valid_matcher.group());
        }
        while(tx_machter.find()){
            tx_timeArray.add(tx_machter.group());
        }

        // Convert String from CSV to a Neo4j(Cypher) instance
        Path path = Paths.get(dbPath);
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(path).build();
        graphDb = managementService.database( DEFAULT_DATABASE_NAME );
        registerShutdownHook(managementService);

        try ( Transaction tx = graphDb.beginTx() ){

            tempNode = tx.createNode();
            tempNode.setProperty("e_id", temp[0].hashCode());
            tempNode.setProperty("g_id", temp[1].hashCode());
            tempNode.setProperty("sid", temp[2].hashCode());
            tempNode.setProperty("tid", temp[3].hashCode());
            tempNode.setProperty("e_lable", temp[4]);
            tempNode.setProperty("properties", temp[5]);
            tempNode.setProperty("v_from", v_timeArray.get(0));
            tempNode.setProperty("v_to", v_timeArray.get(1));
            tempNode.setProperty("tx_from", tx_timeArray.get(0));
            tempNode.setProperty("tx_to", tx_timeArray.get(1));

            //System.out.print(tempNode.getAllProperties());
            tx.commit();
        }

        managementService.shutdown();

    }

    private static void registerShutdownHook( final DatabaseManagementService managementService )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                managementService.shutdown();
            }
        } );
    }
}
