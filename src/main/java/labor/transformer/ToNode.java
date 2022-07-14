package labor.transformer;

import labor.nodeRel.DegreeTypeRel;
import labor.nodeRel.DimensionTypeRel;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class ToNode {
    /* Consider use the DegreeType as the relationship, since it can be used as the param for the method getDegree(RelationshipType relationshipType)
    Relationship relationship;
     */
    private final DegreeTypeRel degreeType;
    private final DimensionTypeRel dimensionTypeRel;

    /**
     * @param degreeType IN, OUT or BOTH
     * @param dimensionTypeRel VALID_TIME or TRANSACTION_TIME
     */
    public ToNode(DegreeTypeRel degreeType, DimensionTypeRel dimensionTypeRel){
        this.degreeType = Objects.requireNonNull(degreeType);
        this.dimensionTypeRel = Objects.requireNonNull(dimensionTypeRel);
    }

    public void stringToNode(String filePath_read, String dbPath){
        // Get in touch with Neo4j
        Path path = Paths.get(dbPath);
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(path).build();
        GraphDatabaseService graphDb = managementService.database( DEFAULT_DATABASE_NAME );
        registerShutdownHook(managementService);

        // Read the Strings from CSV via Stream
        Path csvPath = Paths.get(filePath_read);

        // Add the Nodes to database
        if (Files.exists(csvPath)) {
            try (Stream<String> tempStream = Files.lines(csvPath)) {

                    tempStream.forEach(s-> convert(graphDb,s));

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Queries
        try ( Transaction tx = graphDb.beginTx() ) {
            System.out.println("Query start");
            String q = "MATCH(n {VertexId: 1072896730}) RETURN n.VertexId, n.from, n.to";
            Result result = tx.execute(q);
            String n = result.resultAsString();
            System.out.println(n);
        }

        // Turn off
        managementService.shutdown();
    }

    private void convert(GraphDatabaseService graphDb, String str){
        // 1) Handel the String
        String[] temp = str.split(";");
        String[] timeString = temp[temp.length-1].split("\\),\\(");

        String valid_time_string = timeString[1];
        String transaction_time_string = timeString[0];

        // get the time via pattern and matcher
        Pattern pattern = Pattern.compile("-?\\d+");

        Matcher valid_matcher = pattern.matcher(valid_time_string);
        Matcher tx_machter = pattern.matcher(transaction_time_string);

        ArrayList<String> v_timeArray = new ArrayList<>();
        ArrayList<String> tx_timeArray = new ArrayList<>();

        while(valid_matcher.find()){
            v_timeArray.add(valid_matcher.group());
        }
        while(tx_machter.find()){
            tx_timeArray.add(tx_machter.group());
        }

        // get the from and to for different DimensionType
        Long from = dimensionTypeRel.equals(DimensionTypeRel.VALID_TIME) ? Long.valueOf(v_timeArray.get(0)) : Long.valueOf(tx_timeArray.get(0));
        Long to = dimensionTypeRel.equals(DimensionTypeRel.VALID_TIME) ? Long.valueOf(v_timeArray.get(1)) : Long.valueOf(tx_timeArray.get(1));

        // 2) Add to Nodes
        if( degreeType.equals(DegreeTypeRel.IN) ){
            try ( Transaction tx = graphDb.beginTx() ) {
                Node tempNode_IN = tx.createNode();
                // tid
                tempNode_IN.setProperty("VertexId", temp[3].hashCode());
                tempNode_IN.setProperty("from", from);
                tempNode_IN.setProperty("to", to);

                //System.out.println("IN" + tempNode_IN.getAllProperties());
                tx.commit();
            }
        }
/* switch case may not work here
        switch(degreeType){
            case IN:
                try ( Transaction tx = graphDb.beginTx() ) {
                    Node tempNode_IN = tx.createNode();
                    // tid
                    tempNode_IN.setProperty("VertexId", temp[3].hashCode());
                    tempNode_IN.setProperty("from", from);
                    tempNode_IN.setProperty("to", to);

                    //System.out.println("IN" + tempNode_IN.getAllProperties());
                    tx.commit();
                }
                break;
            case OUT:
                try ( Transaction tx = graphDb.beginTx() ) {
                    Node tempNode_OUT = tx.createNode();
                    // sid
                    tempNode_OUT.setProperty("VertexId", temp[2].hashCode());
                    tempNode_OUT.setProperty("from", from);
                    tempNode_OUT.setProperty("to", to);

                    //System.out.println("OUT" + tempNode_OUT.getAllProperties());
                    tx.commit();
                }
                break;
            case BOTH:
                try ( Transaction tx = graphDb.beginTx() ) {
                    Node tempNode_In = tx.createNode();
                    Node tempNode_Out = tx.createNode();
                    // tid & sid
                    tempNode_In.setProperty("VertexId", temp[3].hashCode());
                    tempNode_In.setProperty("from", from);
                    tempNode_In.setProperty("to", to);
                    tempNode_Out.setProperty("VertexId", temp[2].hashCode());
                    tempNode_Out.setProperty("from", from);
                    tempNode_Out.setProperty("to", to);

                    //System.out.println("IN" + tempNode_In.getAllProperties());
                    //System.out.println("OUT" + tempNode_Out.getAllProperties());
                    tx.commit();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid vertex degree type [" + degreeType + "].");
        }
*/
    }

    private void myQuery(GraphDatabaseService graphDb, String query){
        graphDb.executeTransactionally(query);
    }

    private static void registerShutdownHook( final DatabaseManagementService managementService )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread(managementService::shutdown));
    }
}
